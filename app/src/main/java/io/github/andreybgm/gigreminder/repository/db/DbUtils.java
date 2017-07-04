package io.github.andreybgm.gigreminder.repository.db;

import android.database.Cursor;

public class DbUtils {
    public static String getStringFromCursor(Cursor cursor, String column) {
        return cursor.getString(cursor.getColumnIndexOrThrow(column));
    }

    public static long getLongFromCursor(Cursor cursor, String column) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(column));
    }
}
