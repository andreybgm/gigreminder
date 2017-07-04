package io.github.andreybgm.gigreminder.repository.db.entity;

import android.content.ContentValues;
import android.database.Cursor;

import io.github.andreybgm.gigreminder.data.Location;
import io.github.andreybgm.gigreminder.repository.DataSource;
import io.github.andreybgm.gigreminder.repository.db.Contract.LocationsTable;
import io.github.andreybgm.gigreminder.repository.db.DbUtils;

public class LocationEntity extends BaseEntity<Location> {

    private static final String[] PROJECTION = new String[]{
            LocationsTable.COLUMN_ID,
            LocationsTable.COLUMN_NAME,
            LocationsTable.COLUMN_API_CODE
    };

    public LocationEntity(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public String getTableName() {
        return LocationsTable.TABLE_NAME;
    }

    @Override
    public String[] getProjection() {
        return PROJECTION;
    }

    @Override
    public String getIdColumn() {
        return LocationsTable.COLUMN_ID;
    }

    @Override
    public ContentValues toContentValues(Location location) {
        ContentValues values = new ContentValues();
        values.put(LocationsTable.COLUMN_ID, location.getId());
        values.put(LocationsTable.COLUMN_NAME, location.getName());
        values.put(LocationsTable.COLUMN_API_CODE, location.getApiCode());

        return values;
    }

    @Override
    public Location fromCursor(Cursor cursor) {
        String name = DbUtils.getStringFromCursor(cursor, LocationsTable.COLUMN_NAME);
        String id = DbUtils.getStringFromCursor(cursor, LocationsTable.COLUMN_ID);
        String apiCode = DbUtils.getStringFromCursor(cursor, LocationsTable.COLUMN_API_CODE);

        return new Location(id, apiCode, name);
    }
}
