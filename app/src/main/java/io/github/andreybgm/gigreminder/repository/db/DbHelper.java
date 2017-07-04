package io.github.andreybgm.gigreminder.repository.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import io.github.andreybgm.gigreminder.repository.db.Contract.ArtistsTable;
import io.github.andreybgm.gigreminder.repository.db.Contract.ConcertsTable;
import io.github.andreybgm.gigreminder.repository.db.Contract.LocationsTable;
import io.github.andreybgm.gigreminder.repository.db.Contract.SyncStatesTable;

public class DbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "concerts.db";
    private static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE_ARTISTS =
            "CREATE TABLE " + ArtistsTable.TABLE_NAME + "("
                    + ArtistsTable.COLUMN_ID + " TEXT PRIMARY KEY NOT NULL UNIQUE, "
                    + ArtistsTable.COLUMN_NAME + " TEXT NOT NULL UNIQUE"
                    + ")";

    private static final String SQL_CREATE_LOCATIONS =
            "CREATE TABLE " + LocationsTable.TABLE_NAME + "("
                    + LocationsTable.COLUMN_ID + " TEXT PRIMARY KEY NOT NULL UNIQUE, "
                    + LocationsTable.COLUMN_NAME + " TEXT, "
                    + LocationsTable.COLUMN_API_CODE + " TEXT"
                    + ")";

    private static final String SQL_CREATE_CONCERTS =
            "CREATE TABLE " + ConcertsTable.TABLE_NAME + "("
                    + ConcertsTable.COLUMN_ID + " TEXT PRIMARY KEY NOT NULL UNIQUE, "
                    + ConcertsTable.COLUMN_API_CODE + " TEXT, "
                    + ConcertsTable.COLUMN_ARTIST_ID + " TEXT, "
                    + ConcertsTable.COLUMN_LOCATION_ID + " TEXT, "
                    + ConcertsTable.COLUMN_DATE + " INTEGER, "
                    + ConcertsTable.COLUMN_PLACE + " TEXT, "
                    + ConcertsTable.COLUMN_URL + " TEXT, "
                    + ConcertsTable.COLUMN_IMAGE_URL + " TEXT"
                    + ")";

    private static final String SQL_CREATE_SYNC_STATES =
            "CREATE TABLE " + SyncStatesTable.TABLE_NAME + "("
                    + SyncStatesTable.COLUMN_ARTIST_ID + " TEXT, "
                    + SyncStatesTable.COLUMN_LOCATION_ID + " TEXT, "
                    + SyncStatesTable.COLUMN_LAST_SYNC_TIME + " INTEGER, "
                    + "PRIMARY KEY ("
                    + SyncStatesTable.COLUMN_ARTIST_ID + ", "
                    + SyncStatesTable.COLUMN_LOCATION_ID
                    + ")"
                    + ")";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ARTISTS);
        db.execSQL(SQL_CREATE_LOCATIONS);
        db.execSQL(SQL_CREATE_CONCERTS);
        db.execSQL(SQL_CREATE_SYNC_STATES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new IllegalStateException("Upgrade is not allowed");
    }
}
