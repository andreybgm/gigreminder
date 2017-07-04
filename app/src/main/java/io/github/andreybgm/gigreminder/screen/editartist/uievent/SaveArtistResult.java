package io.github.andreybgm.gigreminder.screen.editartist.uievent;

import android.support.annotation.NonNull;

import io.github.andreybgm.gigreminder.screen.base.Result;

public class SaveArtistResult implements Result {
    public static final SaveArtistResult IN_PROGRESS =
            new SaveArtistResult(State.IN_PROGRESS);
    public static final SaveArtistResult SUCCESS =
            new SaveArtistResult(State.SUCCESS);
    public static final SaveArtistResult SAVING_ERROR =
            new SaveArtistResult(State.SAVING_ERROR);

    @NonNull
    private final State state;
    private final boolean nameIsEmpty;
    private final boolean nameIsNotUnique;

    public SaveArtistResult(@NonNull State state) {
        this.state = state;
        this.nameIsEmpty = false;
        this.nameIsNotUnique = false;
    }

    public SaveArtistResult(Builder builder) {
        this.state = builder.state;
        this.nameIsEmpty = builder.nameIsEmpty;
        this.nameIsNotUnique = builder.nameIsNotUnique;
    }

    public boolean isInProgress() {
        return state == State.IN_PROGRESS;
    }

    public boolean isSuccess() {
        return state == State.SUCCESS;
    }

    public boolean isFillError() {
        return state == State.FILL_ERROR;
    }

    public boolean isSavingError() {
        return state == State.SAVING_ERROR;
    }

    public boolean isNameEmpty() {
        return nameIsEmpty;
    }

    public boolean isNameNotUnique() {
        return nameIsNotUnique;
    }

    private enum State {IN_PROGRESS, SUCCESS, FILL_ERROR, SAVING_ERROR}

    public static class Builder {
        @NonNull
        private State state;
        private boolean nameIsEmpty;
        private boolean nameIsNotUnique;

        public static Builder createFillError() {
            return new Builder(State.FILL_ERROR);
        }

        private Builder(@NonNull State state) {
            this.state = state;
        }

        public Builder nameIsEmpty(boolean nameIsEmpty) {
            this.nameIsEmpty = nameIsEmpty;
            return this;
        }

        public Builder nameIsNotUnique(boolean nameIsNotUnique) {
            this.nameIsNotUnique = nameIsNotUnique;
            return this;
        }

        public SaveArtistResult build() {
            return new SaveArtistResult(this);
        }
    }
}
