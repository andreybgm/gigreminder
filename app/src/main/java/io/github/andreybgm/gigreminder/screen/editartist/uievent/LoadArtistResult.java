package io.github.andreybgm.gigreminder.screen.editartist.uievent;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.github.andreybgm.gigreminder.data.Artist;
import io.github.andreybgm.gigreminder.screen.base.event.BooleanResult;
import io.github.andreybgm.gigreminder.utils.Optional;

public class LoadArtistResult extends BooleanResult {
    public static final LoadArtistResult IN_PROGRESS =
            new LoadArtistResult(State.IN_PROGRESS, null, Optional.empty());

    public static LoadArtistResult error(@NonNull Throwable error) {
        return new LoadArtistResult(State.ERROR, error, Optional.empty());
    }

    public static LoadArtistResult success(@NonNull Artist artist) {
        return new LoadArtistResult(State.SUCCESS, null, Optional.of(artist));
    }

    @NonNull
    private final Optional<Artist> artist;

    private LoadArtistResult(@NonNull State state, @Nullable Throwable error,
                             @NonNull Optional<Artist> artist) {
        super(state, error);
        this.artist = artist;
    }

    @NonNull
    public Optional<Artist> getArtist() {
        return artist;
    }
}
