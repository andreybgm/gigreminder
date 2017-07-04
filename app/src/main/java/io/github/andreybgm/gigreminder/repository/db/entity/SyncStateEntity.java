package io.github.andreybgm.gigreminder.repository.db.entity;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.Date;

import io.github.andreybgm.gigreminder.data.Artist;
import io.github.andreybgm.gigreminder.data.Location;
import io.github.andreybgm.gigreminder.data.SyncState;
import io.github.andreybgm.gigreminder.repository.DataSource;
import io.github.andreybgm.gigreminder.repository.db.Contract.SyncStatesTable;
import io.github.andreybgm.gigreminder.repository.db.DbUtils;

public class SyncStateEntity extends BaseEntity<SyncState> {

    private static final String[] PROJECTION = new String[]{
            SyncStatesTable.COLUMN_ARTIST_ID,
            SyncStatesTable.COLUMN_LOCATION_ID,
            SyncStatesTable.COLUMN_LAST_SYNC_TIME
    };

    public SyncStateEntity(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public String getTableName() {
        return SyncStatesTable.TABLE_NAME;
    }

    @Override
    public String[] getProjection() {
        return PROJECTION;
    }

    @Override
    public String getIdColumn() {
        return SyncStatesTable.COLUMN_ID;
    }

    @Override
    public ContentValues toContentValues(SyncState entity) {
        ContentValues values = new ContentValues();
        values.put(SyncStatesTable.COLUMN_ARTIST_ID, entity.getArtist().getId());
        values.put(SyncStatesTable.COLUMN_LOCATION_ID, entity.getLocation().getId());
        values.put(SyncStatesTable.COLUMN_LAST_SYNC_TIME, entity.getLastSync().getTime());

        return values;
    }

    @Override
    public SyncState fromCursor(Cursor cursor) {
        String artistId = DbUtils.getStringFromCursor(cursor, SyncStatesTable.COLUMN_ARTIST_ID);
        String locationId = DbUtils.getStringFromCursor(cursor, SyncStatesTable.COLUMN_LOCATION_ID);
        Date lastSyncTime = new Date(DbUtils.getLongFromCursor(cursor,
                SyncStatesTable.COLUMN_LAST_SYNC_TIME));
        Artist artist = getDataSource().getArtist(artistId).blockingGet();
        Location location = getDataSource().getLocation(locationId).blockingGet();

        return new SyncState(artist, location, lastSyncTime);
    }
}
