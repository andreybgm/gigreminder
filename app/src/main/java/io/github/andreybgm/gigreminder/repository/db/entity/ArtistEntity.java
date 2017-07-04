package io.github.andreybgm.gigreminder.repository.db.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;

import io.github.andreybgm.gigreminder.data.Artist;
import io.github.andreybgm.gigreminder.repository.DataSource;
import io.github.andreybgm.gigreminder.repository.db.Contract.ArtistsTable;
import io.github.andreybgm.gigreminder.repository.db.DbUtils;
import io.github.andreybgm.gigreminder.repository.error.NotUniqueArtistException;

public class ArtistEntity extends BaseEntity<Artist> {

    private static final String[] PROJECTION = new String[]{
            ArtistsTable.COLUMN_ID,
            ArtistsTable.COLUMN_NAME
    };

    public ArtistEntity(DataSource dataSource) {
        super(dataSource);
    }

    public static void handleSqlConstraintException(SQLiteConstraintException e) {
        String msg = e.getMessage().toLowerCase();

        if (msg.contains("unique")
                && msg.contains(String.format("%s.%s",
                ArtistsTable.TABLE_NAME.toLowerCase(),
                ArtistsTable.COLUMN_NAME.toLowerCase()))) {
            throw new NotUniqueArtistException();
        }

        throw e;
    }

    @Override
    public String getTableName() {
        return ArtistsTable.TABLE_NAME;
    }

    @Override
    public String[] getProjection() {
        return PROJECTION;
    }

    @Override
    public String getIdColumn() {
        return ArtistsTable.COLUMN_ID;
    }

    @Override
    public ContentValues toContentValues(Artist artist) {
        ContentValues values = new ContentValues();
        values.put(ArtistsTable.COLUMN_ID, artist.getId());
        values.put(ArtistsTable.COLUMN_NAME, artist.getName());

        return values;
    }

    @Override
    public Artist fromCursor(Cursor cursor) {
        String name = DbUtils.getStringFromCursor(cursor, ArtistsTable.COLUMN_NAME);
        String id = DbUtils.getStringFromCursor(cursor, ArtistsTable.COLUMN_ID);

        return new Artist(id, name);
    }
}
