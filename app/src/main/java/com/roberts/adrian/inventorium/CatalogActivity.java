package com.roberts.adrian.inventorium;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;

import com.roberts.adrian.inventorium.data.ItemContract.ItemEntry;

import java.io.ByteArrayOutputStream;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = CatalogActivity.class.getName();
    CursorAdapter mCursorAdapter;

    /**
     * Identifier for the pet data loader
     */
    private static final int CURSOR_LOADER_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);


        // Setup FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addItem = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(addItem);
            }
        });

        // ListView of inventory. TODO: populate with adapter
        ListView listView = (ListView) findViewById(R.id.list);
        View emptyView = findViewById(R.id.emptyView);
        listView.setEmptyView(emptyView);

        mCursorAdapter = new ItemCursorAdapter(this, null);
        listView.setAdapter(mCursorAdapter);
        ;

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                String appBarTitle = getString(R.string.editor_title_edit_item);
                Uri mItemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id);

                Intent editItem = new Intent(appBarTitle, mItemUri, getBaseContext(), EditorActivity.class);
                startActivity(editItem);
            }
        });
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.catalog_settings_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCursorAdapter.isEmpty()) {
            MenuItem delete_all = menu.findItem(R.id.action_delete_all);
            delete_all.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy:
                insertDummyItem();
                return true;
            case R.id.action_delete_all:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.empty_inventory)
                .setMessage(R.string.alert_dialog_delete_all_msg)
                .setPositiveButton(R.string.alert_dialog_delete_all_button_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteAllItems();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog

                    }
                });
        // Create the AlertDialog object and return it
        builder.create();
        builder.show();
    }

    private void insertDummyItem() {
        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_NAME, "Ultra A4 White Copy Paper Carton");
        values.put(ItemEntry.COLUMN_SUPPLIER, "Redford Paper");
        values.put(ItemEntry.COLUMN_QUANTITY, 10);
        values.put(ItemEntry.COLUMN_PRICE, 2);

        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        values.put(ItemEntry.COLUMN_IMAGE, byteArray);
        values.put(ItemEntry.COLUMN_DATE, "Mar 13, 16");

        Uri dummysUi = getContentResolver().insert(ItemEntry.CONTENT_URI, values);
    }

    private void deleteAllItems() {
        getContentResolver().delete(ItemEntry.CONTENT_URI, null, null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_NAME,
                ItemEntry.COLUMN_SUPPLIER,
                ItemEntry.COLUMN_QUANTITY,
                ItemEntry.COLUMN_PRICE,
                ItemEntry.COLUMN_DATE,
                ItemEntry.COLUMN_IMAGE
        };
        String sortOrder = "" + ItemEntry._ID + " DESC";
        return new CursorLoader(this, ItemEntry.CONTENT_URI, projection, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // If there are no items showing in the list, hide the 'delete all' option
        Log.i(LOG_TAG, "mCursorAdapterCount :" + mCursorAdapter.getCount() + "\ncursorCount: " + cursor.getCount());
        if (mCursorAdapter.isEmpty()) {
            invalidateOptionsMenu();
        }


        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.i(LOG_TAG, "mCursorAdapterEmpty (OLF) : " + mCursorAdapter.isEmpty());
        mCursorAdapter.swapCursor(null);
    }
}
