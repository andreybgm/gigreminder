package io.github.andreybgm.gigreminder.screen.locations.uievent;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.github.andreybgm.gigreminder.screen.base.event.BooleanResult;

public class DeleteLocationResult extends BooleanResult {
    public static final DeleteLocationResult IN_PROGRESS =
            new DeleteLocationResult(State.IN_PROGRESS, null);
    public static final DeleteLocationResult SUCCESS =
            new DeleteLocationResult(State.SUCCESS, null);

    public static DeleteLocationResult error(@NonNull Throwable error) {
        return new DeleteLocationResult(State.ERROR, error);
    }

    private DeleteLocationResult(@NonNull State state, @Nullable Throwable error) {
        super(state, error);
    }
}
