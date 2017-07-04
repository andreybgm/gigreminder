package io.github.andreybgm.gigreminder.screen.artists.uievent;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.github.andreybgm.gigreminder.screen.base.event.BooleanResult;

public class DeleteArtistsResult extends BooleanResult {
    public static final DeleteArtistsResult IN_PROGRESS =
            new DeleteArtistsResult(State.IN_PROGRESS, null);
    public static final DeleteArtistsResult SUCCESS =
            new DeleteArtistsResult(State.SUCCESS, null);

    public static DeleteArtistsResult error(@NonNull Throwable error) {
        return new DeleteArtistsResult(State.ERROR, error);
    }

    private DeleteArtistsResult(@NonNull State state, @Nullable Throwable error) {
        super(state, error);
    }
}
