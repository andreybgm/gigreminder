package io.github.andreybgm.gigreminder.screen.artists.uievent;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.List;

import io.github.andreybgm.gigreminder.data.Artist;
import io.github.andreybgm.gigreminder.screen.base.event.BooleanResult;

public class LoadArtistsResult extends BooleanResult {
    public static final LoadArtistsResult IN_PROGRESS =
            new LoadArtistsResult(State.IN_PROGRESS, null, Collections.emptyList());

    public static LoadArtistsResult error(@NonNull Throwable error) {
        return new LoadArtistsResult(State.ERROR, error, Collections.emptyList());
    }

    public static LoadArtistsResult success(@NonNull List<Artist> artists) {
        return new LoadArtistsResult(State.SUCCESS, null, artists);
    }

    @NonNull
    private final List<Artist> artists;

    private LoadArtistsResult(@NonNull State state, @Nullable Throwable error,
                              @NonNull List<Artist> artists) {
        super(state, error);
        this.artists = artists;
    }

    @NonNull
    public List<Artist> getArtists() {
        return artists;
    }
}
