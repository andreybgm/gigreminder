package io.github.andreybgm.gigreminder.repository.db.entity;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.Date;

import io.github.andreybgm.gigreminder.data.Concert;
import io.github.andreybgm.gigreminder.repository.DataSource;
import io.github.andreybgm.gigreminder.repository.db.Contract.ConcertsTable;
import io.github.andreybgm.gigreminder.repository.db.DbUtils;

public class ConcertEntity extends BaseEntity<Concert> {

    private static final String[] PROJECTION = new String[]{
            ConcertsTable.COLUMN_ID,
            ConcertsTable.COLUMN_API_CODE,
            ConcertsTable.COLUMN_ARTIST_ID,
            ConcertsTable.COLUMN_LOCATION_ID,
            ConcertsTable.COLUMN_DATE,
            ConcertsTable.COLUMN_PLACE,
            ConcertsTable.COLUMN_URL,
            ConcertsTable.COLUMN_IMAGE_URL
    };

    public ConcertEntity(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public String getTableName() {
        return ConcertsTable.TABLE_NAME;
    }

    @Override
    public String[] getProjection() {
        return PROJECTION;
    }

    @Override
    public String getIdColumn() {
        return ConcertsTable.COLUMN_ID;
    }

    @Override
    public ContentValues toContentValues(Concert concert) {
        ContentValues values = new ContentValues();
        values.put(ConcertsTable.COLUMN_ID, concert.getId());
        values.put(ConcertsTable.COLUMN_API_CODE, concert.getApiCode());
        values.put(ConcertsTable.COLUMN_ARTIST_ID, concert.getArtist().getId());
        values.put(ConcertsTable.COLUMN_LOCATION_ID, concert.getLocation().getId());
        values.put(ConcertsTable.COLUMN_DATE, concert.getDate().getTime());
        values.put(ConcertsTable.COLUMN_PLACE, concert.getPlace());
        values.put(ConcertsTable.COLUMN_URL, concert.getUrl());
        values.put(ConcertsTable.COLUMN_IMAGE_URL, concert.getImageUrl());

        return values;
    }

    @Override
    public Concert fromCursor(Cursor cursor) {
        String artistId = DbUtils.getStringFromCursor(cursor,
                ConcertsTable.COLUMN_ARTIST_ID);
        String locationId = DbUtils.getStringFromCursor(cursor,
                ConcertsTable.COLUMN_LOCATION_ID);

        Concert.Builder builder = new Concert.Builder(
                DbUtils.getStringFromCursor(cursor, ConcertsTable.COLUMN_ID),
                DbUtils.getStringFromCursor(cursor, ConcertsTable.COLUMN_API_CODE),
                getDataSource().getArtist(artistId).blockingGet(),
                getDataSource().getLocation(locationId).blockingGet()
        );

        return builder
                .date(new Date(DbUtils.getLongFromCursor(cursor, ConcertsTable.COLUMN_DATE)))
                .place(DbUtils.getStringFromCursor(cursor, ConcertsTable.COLUMN_PLACE))
                .url(DbUtils.getStringFromCursor(cursor, ConcertsTable.COLUMN_URL))
                .imageUrl(DbUtils.getStringFromCursor(cursor, ConcertsTable.COLUMN_IMAGE_URL))
                .build();
    }
}
