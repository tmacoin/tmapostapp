package org.tmacoin.post.android.messaging.persistance;

import android.provider.BaseColumns;

public final class DatabaseContract {

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private DatabaseContract() {}

    /* Inner class that defines the table contents */
    public static class AddressEntry implements BaseColumns {
        public static final String TABLE_NAME = "address";
        public static final String COLUMN_NAME_TMA_ADDRESS = "tma_address";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY," +
                        COLUMN_NAME_TMA_ADDRESS + " TEXT NOT NULL UNIQUE," +
                        COLUMN_NAME_NAME + " TEXT NOT NULL UNIQUE)";

        public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;

    }

}
