package io.github.andreybgm.gigreminder.repository.artistsource;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GoogleMusicSource implements ArtistSource {
    private static final Uri URI = Uri.parse(
            "content://com.google.android.music.MusicContent/audio");
    private static final String[] PROJECTION = {
            MediaStore.Audio.AudioColumns.ARTIST,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media._ID};

    @Override
    public List<String> loadArtists(Context context) {
        try (Cursor cursor = context.getContentResolver().query(
                URI, PROJECTION, null, null, null)) {
            if (cursor == null) {
                throw new IllegalStateException("No cursor was obtained");
            }

            Set<String> artistsSet = new HashSet<>();

            while (cursor.moveToNext()) {
                int columnIndex = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST);
                String artist = cursor.getString(columnIndex);

                if (!artist.isEmpty()) {
                    artistsSet.add(artist);
                }
            }

            List<String> artists = Arrays.asList(artistsSet.toArray(new String[artistsSet.size()]));
            Collections.sort(artists);

            return artists;
        }
    }
}
