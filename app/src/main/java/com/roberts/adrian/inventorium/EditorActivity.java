package com.roberts.adrian.inventorium;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.roberts.adrian.inventorium.data.ItemContract.ItemEntry;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by Adrian on 05/12/2016.
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    static final int CAMERA_PERMISSION_REQUEST = 1349;
    private static final int EXISTING_ITEM_LOADER = 0;
    private static final String LOG_TAG = EditorActivity.class.getName();

    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;

    private EditText mEditTextItemName;
    private EditText mEditTextItemSupplier;
    private EditText mEditTextItemPrice;
    private Spinner mSpinnerQuantity;
    private ImageView mEditItemImage;
    private TextView mQuantityDisplay;
    private String mItemDateAdded;
    private Button mAddImageBtn;
    private NumberPicker mNumberPickerQuantity;
    private Uri mItemUri;

    public boolean mItemHasChanged;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Intent intent = getIntent();
        String appBarTitle = intent.getAction();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setCameraPermissionRequest();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_editor);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(EditorActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.set_image_dialog);
                // dialog.getWindow().setBackgroundDrawableResource(R.color.transparentBackground);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
                //
                Button camera = (Button) dialog.findViewById(R.id.image_camera_button);
                camera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(
                                getPackageManager()) != null &&
                                EasyPermissions.hasPermissions(EditorActivity.this, Manifest.permission.CAMERA)) {

                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                        }
                    }
                });

                Button album = (Button) dialog.findViewById(R.id.image_album_button);
                album.setOnClickListener(new View.OnClickListener()

                {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        Intent albumIntent = new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(albumIntent, RESULT_LOAD_IMAGE);
                    }
                });

            }
        });
        mItemUri = intent.getData();
        Log.i(LOG_TAG, "mItemUri:\n\n\n " + mItemUri);

        // If we're dealing with adding a new pet
        if (mItemUri == null)

        {
            setTitle(getString(R.string.editor_title_add_item));
            invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
        } else

        {
            setTitle(appBarTitle);

            // create loader only if we are editing a existing pet
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        mEditTextItemName = (EditText)

                findViewById(R.id.editor_item_name);

        mEditTextItemSupplier = (EditText)

                findViewById(R.id.editor_item_supplier);

        mEditTextItemPrice = (EditText)

                findViewById(R.id.editor_item_price);
        //   mSpinnerQuantity = (Spinner) findViewById(R.id.editor_item_quantity);
        mQuantityDisplay = (TextView)

                findViewById(R.id.editor_item_quantity_tv);

        mEditItemImage = (ImageView)

                findViewById(R.id.editor_item_image);

        // Getting date
        Date dateObj = new Date();
        dateObj.getTime();
        SimpleDateFormat format = new SimpleDateFormat("MMM dd, yy");
        mItemDateAdded = format.format(dateObj);

        //  mAddImageBtn = (Button) findViewById(R.id.add_image_btn);
        mNumberPickerQuantity = (NumberPicker)

                findViewById(R.id.editor_quantity_numberPicker);
        mNumberPickerQuantity.setMinValue(0);
        mNumberPickerQuantity.setMaxValue(30);
        //      mNumberPickerQuantity.setValue(5);
        mNumberPickerQuantity.setWrapSelectorWheel(false); // Whatis

        mEditItemImage.setOnTouchListener(onTouchListener);
        mEditTextItemName.setOnTouchListener(onTouchListener);
        mEditTextItemSupplier.setOnTouchListener(onTouchListener);
        mEditTextItemPrice.setOnTouchListener(onTouchListener);
        mNumberPickerQuantity.setOnTouchListener(onTouchListener);
        mNumberPickerQuantity.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()

        {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
                // TODO passe på at TV multiplikerer riktig
                // If user is editing an item we want 'In stock' to stay
                if (mItemUri == null) {
                    mQuantityDisplay.setText(String.valueOf(newValue));
                }
            }
        });
    }

    @AfterPermissionGranted(CAMERA_PERMISSION_REQUEST)
    private void setCameraPermissionRequest() {
        if (!EasyPermissions.hasPermissions(getApplicationContext(), Manifest.permission.CAMERA)) {
            EasyPermissions.requestPermissions(this, "Camera permissions required in order to use your camera to store images", CAMERA_PERMISSION_REQUEST, Manifest.permission.CAMERA);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor_settings_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_save:
                saveItem();
                // Dialog
                finish();
                return true;
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                Log.i(LOG_TAG, "Has changed: " + mItemHasChanged);
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // Hide 'delete' when user is creating a new item
        if (mItemUri == null) {
            MenuItem delete = menu.findItem(R.id.action_delete);
            delete.setVisible(false);
        }
        return true;
    }

    private void deleteItem() {
        // Only perform deletion if item exists
        if (mItemUri != null) {
            int rowsDeleted = getContentResolver().delete(mItemUri, null, null);

            // Toast describing deletion success
            if (rowsDeleted == 0) {
                Toast.makeText(this, R.string.editor_item_deletion_fail, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.editor_item_deletion_success, Toast.LENGTH_SHORT).show();
            }
            // Close activity (back to Catalog)
            finish();
        }
    }


    /**
     * Setting image from either camera capture or album to imageview
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap userImage;
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            // Bitmap of image given by user
            // userImage = BitmapFactory.decodeFile(picturePath);
            // Log size of that bitmap
            // Log.i(LOG_TAG, "bm size before: " + byteSizeOf(userImage));

            // **Update** using decodeStream instead
            // userImage = ImageResizer.decodeSampledBitmapFromFile(picturePath, 200, 200);
            try {
                userImage = BitmapFactory.decodeStream(this.getContentResolver().openInputStream(data.getData()));
                mEditItemImage.setImageBitmap(userImage);
            } catch (FileNotFoundException e) {
                Toast.makeText(this, "Error getting image", Toast.LENGTH_SHORT).show();
            }

            ////setImageBitmap(BitmapFactory.decodeFile(picturePath));
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && null != data) {

            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mEditItemImage.setImageBitmap(imageBitmap);


        }

    }

    /**
     * returns the bytesize of the give bitmap
     */
    public static int byteSizeOf(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return bitmap.getAllocationByteCount();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return bitmap.getByteCount();
        } else {
            return bitmap.getRowBytes() * bitmap.getHeight();
        }
    }

    private void saveItem() {
        ContentValues values = new ContentValues();
        String itemName = mEditTextItemName.getText().toString().trim();
        String itemSupplier = mEditTextItemSupplier.getText().toString().trim();
        Integer itemQuantity = mNumberPickerQuantity.getValue();
        Double itemPrice = 0.0;
        try {
            itemPrice = Double.parseDouble(mEditTextItemPrice.getText().toString());
        } catch (NumberFormatException | NullPointerException e) {
            e.printStackTrace();
        }

        // Retrieve image from imageview if any exists
        if (mEditItemImage.getDrawable() != null) {
            Bitmap itemBitmap = ((BitmapDrawable) mEditItemImage.getDrawable()).getBitmap();
            Log.e(LOG_TAG, "bitmap null: " + (itemBitmap == null));
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            if (itemBitmap != null) itemBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            values.put(ItemEntry.COLUMN_IMAGE, byteArray);
        }
        // If user is in 'Add Item mode' and clicks 'save' without any values, return
        if (mItemUri == null
                && TextUtils.isEmpty(itemName)
                && TextUtils.isEmpty(itemSupplier)
                && TextUtils.isEmpty(String.valueOf(itemQuantity))
                && TextUtils.isEmpty(String.valueOf(itemPrice))) {
            return;
        }
        values.put(ItemEntry.COLUMN_NAME, itemName);
        values.put(ItemEntry.COLUMN_SUPPLIER, itemSupplier);
        values.put(ItemEntry.COLUMN_QUANTITY, itemQuantity);
        values.put(ItemEntry.COLUMN_PRICE, itemPrice);
        values.put(ItemEntry.COLUMN_DATE, mItemDateAdded);
        // If the item is newly added we want to insert
        if (mItemUri == null) {
            //TODO: IMAGE STUFF  values.put(ItemEntry.COLUMN_IMAGE, mEditTextItemName.toString());
            Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);
            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, itemName + " " + getString(R.string.editor_insert_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Or update an existing item
        else {
            int rowsAffected = getContentResolver().update(mItemUri, values, null, null);
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_pet_successful) + "\n" +
                                "Name: " + mEditTextItemName.getText().toString(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    // If the item hasn't been editing, pressing back is good to go
    @Override
    public void onBackPressed() {
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }
        // If it has been edited, show dialog
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "discard", exit current activity
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this pet.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                AlertDialog alertDialog = new AlertDialog.Builder(EditorActivity.this).create();
                alertDialog.setTitle("Why do you want to delete me?");
                alertDialog.setMessage(getString(R.string.delete_dialog_msg_beyond));
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "I don't really want to delete",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "I just want to",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                deleteItem();
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();

            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_NAME,
                ItemEntry.COLUMN_SUPPLIER,
                ItemEntry.COLUMN_PRICE,
                ItemEntry.COLUMN_QUANTITY,
                ItemEntry.COLUMN_DATE,
                ItemEntry.COLUMN_IMAGE
        };

        return new CursorLoader(this, mItemUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Return early if cursor is null or has no data stored
        if (cursor == null || cursor.getColumnCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of item attributes that we're interested in
            Log.i(LOG_TAG, "Move to first == true");
            int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_NAME);
            int supplierColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_SUPPLIER);
            int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_PRICE);
            int imageColumnInex = cursor.getColumnIndex(ItemEntry.COLUMN_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);

            // Get image if it exists
            Bitmap image = null;
            if (cursor.getBlob(imageColumnInex) != null) {
                byte[] imgAsByteArray = cursor.getBlob(imageColumnInex);
                image = BitmapFactory.decodeByteArray(imgAsByteArray, 0, imgAsByteArray.length);
            }
            /**
             *  Update the views on the screen with the values from the database
             */

            TextView numberPickerLabel = (TextView) findViewById(R.id.editor_quantity_numberPicker_label);
            numberPickerLabel.setText("Edit amount:");

            mEditTextItemName.setText(name);
            mEditTextItemSupplier.setText(supplier);
            mQuantityDisplay.setText(Integer.toString(quantity));
            mEditTextItemPrice.setText(Double.toString(price));
            mNumberPickerQuantity.setValue(quantity);
            mEditItemImage.setImageBitmap(image);
            Log.i(LOG_TAG, "Name / supplier :" + name + " / " + supplier);

/*
            // Set Add image-button text according to edit/add mode
            if (mEditItemImage.getDrawable() == null) {
                mAddImageBtn.setText("ADD IMAGE");
            } else {
                mAddImageBtn.setText("CHANGE IMAGE");
            }
*/

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mEditTextItemName.setText("");
        mEditTextItemSupplier.setText("");
        mQuantityDisplay.setText("");
        mEditTextItemPrice.setText("");
        mEditItemImage.setImageBitmap(null);
    }
}
