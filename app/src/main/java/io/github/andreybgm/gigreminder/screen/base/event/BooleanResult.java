package io.github.andreybgm.gigreminder.screen.base.event;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.github.andreybgm.gigreminder.screen.base.Result;

public class BooleanResult implements Result {
    @NonNull
    private final State state;
    @Nullable
    private final Throwable error;

    public BooleanResult(@NonNull State state, @Nullable Throwable error) {
        this.state = state;
        this.error = error;
    }

    public Throwable getError() {
        if (state != State.ERROR || error == null) {
            throw new IllegalStateException("There's no error");
        }

        return error;
    }

    public boolean isInProgress() {
        return state == State.IN_PROGRESS;
    }

    public boolean isSuccess() {
        return state == State.SUCCESS;
    }

    public boolean isError() {
        return state == State.ERROR;
    }

    protected enum State {IN_PROGRESS, SUCCESS, ERROR}
}
