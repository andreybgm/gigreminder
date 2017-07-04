package io.github.andreybgm.gigreminder.repository.db;

public class Contract {

    public static class ArtistsTable {
        public static final String TABLE_NAME = "artists";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_ID = "entryid";
    }

    public static class LocationsTable {
        public static final String TABLE_NAME = "locations";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_ID = "entryid";
        public static final String COLUMN_API_CODE = "apicode";
    }

    public static class ConcertsTable {
        public static final String TABLE_NAME = "concerts";
        public static final String COLUMN_ID = "entryid";
        public static final String COLUMN_API_CODE = "apicode";
        public static final String COLUMN_ARTIST_ID = "artist";
        public static final String COLUMN_LOCATION_ID = "location";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_PLACE = "place";
        public static final String COLUMN_URL = "url";
        public static final String COLUMN_IMAGE_URL = "imageurl";
    }

    public static class SyncStatesTable {
        public static final String TABLE_NAME = "syncstates";
        public static final String COLUMN_ID = "rowid";
        public static final String COLUMN_ARTIST_ID = "artist";
        public static final String COLUMN_LOCATION_ID = "location";
        public static final String COLUMN_LAST_SYNC_TIME = "lastsynctime";
    }
}
