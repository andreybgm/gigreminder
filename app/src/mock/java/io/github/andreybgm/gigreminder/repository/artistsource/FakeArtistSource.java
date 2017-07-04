package io.github.andreybgm.gigreminder.repository.artistsource;

import android.content.Context;

import java.util.List;

import io.reactivex.Observable;

public class FakeArtistSource implements ArtistSource {

    public static final int ARTIST_COUNT = 10;
    private static final String ARTIST_PREFIX = "Artist";
    private static boolean throwError;

    public static String generateName(int n) {
        return ARTIST_PREFIX + n;
    }

    public static void setThrowError(boolean throwError) {
        FakeArtistSource.throwError = throwError;
    }

    @Override
    public List<String> loadArtists(Context context) {
        if (throwError) {
            throw new RuntimeException();
        }

        return Observable.range(1, ARTIST_COUNT)
                .map(FakeArtistSource::generateName)
                .toList()
                .blockingGet();
    }
}
