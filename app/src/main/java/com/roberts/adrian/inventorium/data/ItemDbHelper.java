package com.roberts.adrian.inventorium.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.roberts.adrian.inventorium.data.ItemContract.ItemEntry;

/**
 * Created by Adrian on 05/12/2016.
 */

public class ItemDbHelper extends SQLiteOpenHelper {
    final static String LOG_TAG = ItemDbHelper.class.getName();
    /**
     * Name of database
     */
    public static final String DATABASE_NAME = "inventory.db";

    /**
     * Database version. Remember to increment version upon schema change
     */
    public static int DATABASE_VERSION = 18;

    public ItemDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_ITEMS_TABLE = "CREATE TABLE " + ItemEntry.TABLE_NAME + " ("
                + ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ItemEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + ItemEntry.COLUMN_SUPPLIER + " TEXT NOT NULL, "
                + ItemEntry.COLUMN_QUANTITY + " INTEGER DEFAULT 0, "
                + ItemEntry.COLUMN_PRICE + " DOUBLE NOT NULL DEFAULT 0, "
                + ItemEntry.COLUMN_IMAGE + " BLOB, "
                + ItemEntry.COLUMN_DATE + " TEXT);";
        db.execSQL(SQL_CREATE_ITEMS_TABLE);
        Log.i(LOG_TAG, "DB TOSTRING: " + db.toString());
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + ItemEntry.TABLE_NAME);
        Log.i(LOG_TAG, "DB onUpgrade, DB VERION: " + DATABASE_VERSION);
        onCreate(db);
    }
}
