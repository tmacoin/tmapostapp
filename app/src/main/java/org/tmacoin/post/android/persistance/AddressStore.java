package org.tmacoin.post.android.persistance;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

public class AddressStore {

    private Context context;
    TmaPostDbHelper dbHelper;
    private SQLiteDatabase db;

    public AddressStore(Context context) {
        this.context = context;
        dbHelper = new TmaPostDbHelper(context);
        db = dbHelper.getWritableDatabase();

    }

    public void save(Address address) {
        String name = findNameByTmaAddress(address.getTmaAddress());
        if(address.getName().equals(name)) {
            return;
        }
        if(name == null) {
            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(DatabaseContract.AddressEntry.COLUMN_NAME_TMA_ADDRESS, address.getTmaAddress());
            values.put(DatabaseContract.AddressEntry.COLUMN_NAME_NAME, address.getName());

            // Insert the new row, returning the primary key value of the new row
            long newRowId = db.insert(DatabaseContract.AddressEntry.TABLE_NAME, null, values);
            return;
        }
        update(address);
    }

    public void update(Address address) {
        // New value for one column
        String name = findNameByTmaAddress(address.getTmaAddress());
        if(address.getName().equals(name)) {
            return;
        }
        if(name == null) {
            save(address);
            return;
        }
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.AddressEntry.COLUMN_NAME_NAME, address.getName());

        // Which row to update, based on the title
        String selection = DatabaseContract.AddressEntry.COLUMN_NAME_TMA_ADDRESS + " LIKE ?";
        String[] selectionArgs = { address.getTmaAddress() };

        int count = db.update(
                DatabaseContract.AddressEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }

    public String findNameByTmaAddress(String tmaAddress) {
        String name = null;
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                BaseColumns._ID,
                DatabaseContract.AddressEntry.COLUMN_NAME_TMA_ADDRESS,
                DatabaseContract.AddressEntry.COLUMN_NAME_NAME
        };

        // Filter results WHERE "tmaAddress" = 'tmaAddress'
        String selection = DatabaseContract.AddressEntry.COLUMN_NAME_TMA_ADDRESS + " = ?";
        String[] selectionArgs = {tmaAddress};

        // How you want the results sorted in the resulting Cursor
        String sortOrder = DatabaseContract.AddressEntry.COLUMN_NAME_TMA_ADDRESS + " DESC";

        Cursor cursor = db.query(
                DatabaseContract.AddressEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        if(cursor.moveToNext()) {
            name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.AddressEntry.COLUMN_NAME_NAME));
        }
        cursor.close();
        return name;
    }

    public String[] getAllNames() {
        List<String> names = new ArrayList<>();
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                BaseColumns._ID,
                DatabaseContract.AddressEntry.COLUMN_NAME_TMA_ADDRESS,
                DatabaseContract.AddressEntry.COLUMN_NAME_NAME
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = DatabaseContract.AddressEntry.COLUMN_NAME_NAME + " DESC";

        Cursor cursor = db.query(
                DatabaseContract.AddressEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        while(cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.AddressEntry.COLUMN_NAME_NAME));
            names.add(name);
        }
        cursor.close();
        return names.toArray(new String[0]);
    }

    public String findTmaAddressByName(String name) {
        String tmaAddress = null;
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                BaseColumns._ID,
                DatabaseContract.AddressEntry.COLUMN_NAME_TMA_ADDRESS,
                DatabaseContract.AddressEntry.COLUMN_NAME_NAME
        };

        // Filter results WHERE "name" = 'name'
        String selection = DatabaseContract.AddressEntry.COLUMN_NAME_NAME + " = ?";
        String[] selectionArgs = {name};

        // How you want the results sorted in the resulting Cursor
        String sortOrder = DatabaseContract.AddressEntry.COLUMN_NAME_NAME + " DESC";

        Cursor cursor = db.query(
                DatabaseContract.AddressEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        if(cursor.moveToNext()) {
            tmaAddress = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.AddressEntry.COLUMN_NAME_TMA_ADDRESS));
        }
        cursor.close();
        return tmaAddress;
    }

    public void onDestroy() {
        dbHelper.close();
    }


    public void deleteByTmaAddress(String tmaAddress) {

        // Filter results WHERE "tmaAddress" = 'tmaAddress'
        String selection = DatabaseContract.AddressEntry.COLUMN_NAME_TMA_ADDRESS + " = ?";
        String[] selectionArgs = {tmaAddress};

        db.delete(DatabaseContract.AddressEntry.TABLE_NAME, selection, selectionArgs);
    }
}
