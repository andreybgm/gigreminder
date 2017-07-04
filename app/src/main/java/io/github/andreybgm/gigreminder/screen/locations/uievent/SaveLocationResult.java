package io.github.andreybgm.gigreminder.screen.locations.uievent;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.github.andreybgm.gigreminder.screen.base.event.BooleanResult;

public class SaveLocationResult extends BooleanResult {
    public static final SaveLocationResult IN_PROGRESS =
            new SaveLocationResult(State.IN_PROGRESS, null);
    public static final SaveLocationResult SUCCESS =
            new SaveLocationResult(State.SUCCESS, null);

    public static SaveLocationResult error(@NonNull Throwable error) {
        return new SaveLocationResult(State.ERROR, error);
    }

    private SaveLocationResult(@NonNull State state, @Nullable Throwable error) {
        super(state, error);
    }
}
