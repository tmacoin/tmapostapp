package org.tmacoin.post.android.persistance;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import org.tma.peer.thin.SecureMessage;

import java.util.ArrayList;
import java.util.List;

public class MessageStore {

    private Context context;
    TmaPostDbHelper dbHelper;
    private SQLiteDatabase db;

    public MessageStore(Context context) {
        this.context = context;
        dbHelper = new TmaPostDbHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    public void save(String transactionId) {
        String result = findTransactionId(transactionId);
        if(result != null) {
            return;
        }
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.MessageEntry.COLUMN_NAME_TRANSACTION_ID, transactionId);
        long newRowId = db.insert(DatabaseContract.MessageEntry.TABLE_NAME, null, values);
    }

    public void deleteByTransactionId(String transactionId) {
        String selection = DatabaseContract.MessageEntry.COLUMN_NAME_TRANSACTION_ID + " = ?";
        String[] selectionArgs = {transactionId};
        db.delete(DatabaseContract.MessageEntry.TABLE_NAME, selection, selectionArgs);
    }

    public String findTransactionId(String transactionId) {
        String result = null;
        String[] projection = {
                BaseColumns._ID,
                DatabaseContract.MessageEntry.COLUMN_NAME_TRANSACTION_ID
        };

        String selection = DatabaseContract.MessageEntry.COLUMN_NAME_TRANSACTION_ID + " = ?";
        String[] selectionArgs = {transactionId};

        String sortOrder = DatabaseContract.MessageEntry.COLUMN_NAME_TRANSACTION_ID + " DESC";

        Cursor cursor = db.query(
                DatabaseContract.MessageEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        if(cursor.moveToNext()) {
            result = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MessageEntry.COLUMN_NAME_TRANSACTION_ID));
        }
        cursor.close();
        return result;
    }

    public List<String> getAll() {
        List<String> list = new ArrayList<>();
        String[] projection = {
                BaseColumns._ID,
                DatabaseContract.MessageEntry.COLUMN_NAME_TRANSACTION_ID
        };

        String sortOrder = DatabaseContract.MessageEntry.COLUMN_NAME_TRANSACTION_ID + " DESC";

        Cursor cursor = db.query(
                DatabaseContract.MessageEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        while(cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MessageEntry.COLUMN_NAME_TRANSACTION_ID));
            list.add(name);
        }
        cursor.close();
        return list;
    }

    public void onDestroy() {
        dbHelper.close();
    }
}
