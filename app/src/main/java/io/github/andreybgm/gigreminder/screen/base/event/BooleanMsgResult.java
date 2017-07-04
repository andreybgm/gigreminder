package io.github.andreybgm.gigreminder.screen.base.event;

import android.support.annotation.NonNull;

import io.github.andreybgm.gigreminder.screen.base.Result;
import io.github.andreybgm.gigreminder.utils.Optional;

public class BooleanMsgResult implements Result {
    @NonNull
    private final State state;
    @NonNull
    private final Optional<Integer> errorMsg;

    public BooleanMsgResult(@NonNull State state) {
        this(state, Optional.empty());
    }

    public BooleanMsgResult(@NonNull State state, @NonNull Optional<Integer> errorMsg) {
        this.state = state;
        this.errorMsg = errorMsg;
    }

    public int getError() {
        if (state != State.ERROR || !errorMsg.isPresent()) {
            throw new IllegalStateException("There's no error");
        }

        return errorMsg.getValue();
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
