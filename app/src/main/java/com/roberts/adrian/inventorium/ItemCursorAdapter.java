package com.roberts.adrian.inventorium;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.roberts.adrian.inventorium.data.ItemContract.ItemEntry;

/**
 * Created by Adrian on 07/12/2016.
 */

public class ItemCursorAdapter extends CursorAdapter {
    static final String LOG_TAG = ItemCursorAdapter.class.getName();

    public ItemCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView itemName = (TextView) view.findViewById(R.id.catalog_item_name);
        TextView itemSupplier = (TextView) view.findViewById(R.id.catalog_item_supplier);
        TextView itemPrice = (TextView) view.findViewById(R.id.catalog_item_price);
        TextView itemQuantity = (TextView) view.findViewById(R.id.catalog_item_quantity);
        ImageView itemImage = (ImageView) view.findViewById(R.id.catalog_item_image);
        TextView itemDateAdded = (TextView) view.findViewById(R.id.item_added_date);

        // Set the adapter to display the latest added first BETTER YET: ORDER BY
       /* int moveTo = cursor.getCount() - 1 - cursor.getPosition();
        cursor.moveToPosition(moveTo);*/
        Log.i(LOG_TAG, "getCount: " + cursor.getCount() + "\ngetPos: " + cursor.getPosition());
        int nameIndex = cursor.getColumnIndex(ItemEntry.COLUMN_NAME);
        int priceIndex = cursor.getColumnIndex(ItemEntry.COLUMN_PRICE);
        int indexQuantity = cursor.getColumnIndex(ItemEntry.COLUMN_QUANTITY);
        int supplierIndex = cursor.getColumnIndex(ItemEntry.COLUMN_SUPPLIER);
        int imageIndex = cursor.getColumnIndex(ItemEntry.COLUMN_IMAGE);
        int idIndex = cursor.getColumnIndex(ItemEntry._ID);
        int dateIndex = cursor.getColumnIndex(ItemEntry.COLUMN_DATE);

        String name = "";
        String supplier = "";
        Integer quantity = 0;
        Integer id = 0;
        double price = 0.0;
        String itemDate = "";
        try {
            name = cursor.getString(nameIndex);
            supplier = cursor.getString(supplierIndex);
            quantity = cursor.getInt(indexQuantity);
            id = cursor.getInt(idIndex);
            price = cursor.getDouble(priceIndex);
            itemDate = cursor.getString(dateIndex);
        } catch (NullPointerException | IllegalStateException e) {
            e.printStackTrace();
        }


      /*  Date date = new Date();
        date.getTime();
        SimpleDateFormat format = new SimpleDateFormat("MMM dd, yy");
        String itemDate = format.format(date);*/

        itemDateAdded.setText(itemDate);
        itemName.setText(name);
        itemSupplier.setText(supplier);
        itemPrice.setText(Double.toString(price));


        // Altering coloring in displaying quantity  FORENKLE/BEDRE?
        TextView inStockTv = (TextView) view.findViewById(R.id.in_stock_label_tv);
        if (quantity < 1) {
            inStockTv.setTextColor(Color.RED);
            inStockTv.setText(R.string.out_of_stock_list);
            itemQuantity.setVisibility(View.INVISIBLE);
        } else if (quantity < 10) {
            inStockTv.setText("In Stock");
            inStockTv.setTextColor(Color.parseColor("#455A64"));
            itemQuantity.setVisibility(View.VISIBLE);
            itemQuantity.setTextColor(Color.RED);
        } else {
            itemQuantity.setTextColor(view.getResources().getColor(R.color.editorColorPrimaryDark));
        }
        itemQuantity.setText(Integer.toString(quantity));

        try {
            if (cursor.getBlob(imageIndex) != null) {
                byte[] byteArray = cursor.getBlob(imageIndex);
                Bitmap bm = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                Bitmap itemThumbnail = ThumbnailUtils.extractThumbnail(bm, 200, 160);
                itemImage.setImageBitmap(itemThumbnail);
            }
        } catch (IllegalStateException | NullPointerException e) {
            e.printStackTrace();
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
}
