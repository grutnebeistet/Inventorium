package com.roberts.adrian.inventorium.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.roberts.adrian.inventorium.data.ItemContract.ItemEntry;

/**
 * Created by Adrian on 05/12/2016.
 */

public class ItemContentProvider extends ContentProvider {
    private final static String LOG_TAG = ItemContentProvider.class.getName();

    /**
     * URI matcher codes for the content URI:
     * ITEMS for general table query
     * ITEM_ID for specific item
     */
    private static final int ITEMS = 100;
    private static final int ITEM_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.roberts.adrian.inventorium" will map to the
        // integer code {@link #ITEMS}. This URI is used to provide access to MULTIPLE rows
        // of the items table.
        uriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ITEMS, ITEMS);

        // For mapping to individual item with ID
        uriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ITEMS + "/#", ITEM_ID);
    }

    /**
     * Database helper object
     */
    private ItemDbHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new ItemDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selArgs, String sortOrder) {
        // Get readable database
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Cursor to hold the result of the query
        Cursor cursor;
        Log.i(LOG_TAG, "query!! on: " + uri);
        // Checking uri for match type
        int match = uriMatcher.match(uri);
        switch (match) {
            case ITEM_ID:
                selection = ItemEntry._ID + "=?";
                selArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(ItemEntry.TABLE_NAME, projection, selection, selArgs, null, null, sortOrder);
                break;
            case ITEMS:
                cursor = db.query(ItemEntry.TABLE_NAME, projection, selection, selArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query given uri " + uri);
        }
        // Set notification URI on the Cursor
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return ItemEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return ItemEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion failed for " + uri);
        }
    }

    /**
     * @param uri
     * @param values
     * @return uri for newly added item - with id
     */
    public Uri insertItem(Uri uri, ContentValues values) {
        String itemName = values.getAsString(ItemEntry.COLUMN_NAME);
    /*    if (itemName.isEmpty() || itemName == null) {
            throw new IllegalArgumentException("Item must have a name label");
        }*/

        String itemSupplier = values.getAsString(ItemEntry.COLUMN_SUPPLIER);
/*        if (itemSupplier.isEmpty() || itemSupplier == null) {
            throw new IllegalArgumentException("Item must have a supplier");
        }*/

        Double itemPrice = values.getAsDouble(ItemEntry.COLUMN_PRICE);
     /*   if (itemPrice == 0 || itemName.isEmpty()) {
            throw new IllegalArgumentException("Item should not be free of charge");
        }*/
        /**
         *  Quantity can be empty since it may be useful to add a new item before knowing count
         */
//        Integer itemQuantity = values.getAsInteger(ItemEntry.COLUMN_QUANTITY);

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long newRowId = db.insert(ItemEntry.TABLE_NAME, null, values);
        if (newRowId == -1) {
            Log.e(LOG_TAG, "insertion failed for " + uri);
            return null;
        }
        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);
        Log.i(LOG_TAG, "ContentUri w/ ID for INSERTed: \n" + ContentUris.withAppendedId(uri, newRowId)
       + "\n Name / ID : " + itemName + "// " + newRowId);
        return ContentUris.withAppendedId(uri, newRowId);
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted;
        int match = uriMatcher.match(uri);
        switch (match) {
            case ITEM_ID:
                String selection = ItemEntry._ID + "=?";
                String[] selArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(ItemEntry.TABLE_NAME, selection, selArgs);
                break;
            case ITEMS:
                rowsDeleted = db.delete(ItemEntry.TABLE_NAME, s, strings);
                break;
            default:
                throw new IllegalArgumentException("Couldn't delete from uri: " + uri);
        }
        // If deletion  then notify listeners that the data at given uri has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        int match = uriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return updateItem(uri, contentValues, selection, selectionArgs);
            case ITEM_ID:
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Cannot update for given uri " + uri);
        }
    }

    public int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Check that values are valid upon item update
        if (values.containsKey(ItemEntry.COLUMN_NAME)) {
            String itemName = values.getAsString(ItemEntry.COLUMN_NAME);
         /*   if (itemName.isEmpty() || itemName == null) {
                throw new IllegalArgumentException("Item must have a name label");
            }*/
        }
        if (values.containsKey(ItemEntry.COLUMN_SUPPLIER)) {
            String itemSupplier = values.getAsString(ItemEntry.COLUMN_SUPPLIER);
           /* if (itemSupplier.isEmpty() || itemSupplier == null) {
                throw new IllegalArgumentException("Item must have a supplier");
            }*/
        }
        if (values.containsKey(ItemEntry.COLUMN_PRICE)) {
            Double itemPrice = values.getAsDouble(ItemEntry.COLUMN_PRICE);
      /*      if (itemPrice == null || itemPrice == 0) {
                throw new IllegalArgumentException("Item should not be free of charge");
            }*/
        }
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsUpdated = db.update(ItemEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    //TODO: Test if contentvalues are fit for db

}
