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

    public void deleteByTransactionId(String transactionId) {

        // Filter results WHERE "transactionId" = 'transactionId'
        String selection = DatabaseContract.MessageEntry.COLUMN_NAME_TRANSACTION_ID + " = ?";
        String[] selectionArgs = {transactionId};

        db.delete(DatabaseContract.MessageEntry.TABLE_NAME, selection, selectionArgs);
    }

    public void onDestroy() {
        dbHelper.close();
    }
}
