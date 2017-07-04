package io.github.andreybgm.gigreminder.screen.editartist;

import android.support.annotation.NonNull;

import io.github.andreybgm.gigreminder.data.Artist;
import io.github.andreybgm.gigreminder.screen.base.UiModel;
import io.github.andreybgm.gigreminder.utils.Optional;

public class EditArtistUiModel implements UiModel {

    public static final EditArtistUiModel NEW_ARTIST = Builder.create()
            .artistIsNew(true)
            .build();

    private final boolean artistIsNew;
    private final boolean loading;
    private final boolean loadingError;
    private final boolean viewDataIsFilled;
    @NonNull
    private final Optional<String> artistId;
    @NonNull
    private final Optional<Artist> initialArtist;
    private final boolean saving;
    private final boolean savingError;
    private final boolean fillError;
    private final boolean emptyNameError;
    private final boolean notUniqueNameError;
    private final boolean shouldClose;
    private final boolean shouldAskToDiscard;
    @NonNull
    private final Optional<Integer> discardMsg;

    private EditArtistUiModel(Builder builder) {
        this.artistIsNew = builder.artistIsNew;
        this.loading = builder.loading;
        this.loadingError = builder.loadingError;
        this.viewDataIsFilled = builder.viewDataIsFilled;
        this.artistId = builder.artistId;
        this.initialArtist = builder.initialArtist;
        this.saving = builder.saving;
        this.savingError = builder.savingError;
        this.fillError = builder.fillError;
        this.emptyNameError = builder.emptyNameError;
        this.notUniqueNameError = builder.notUniqueNameError;
        this.shouldClose = builder.shouldClose;
        this.shouldAskToDiscard = builder.shouldAskToDiscard;
        this.discardMsg = builder.discardMsg;
    }

    public Builder copy() {
        return new Builder(this);
    }

    public boolean isArtistNew() {
        return artistIsNew;
    }

    public boolean isLoading() {
        return loading;
    }

    public boolean isLoadingError() {
        return loadingError;
    }

    public boolean isViewDataIsFilled() {
        return viewDataIsFilled;
    }

    @NonNull
    public Optional<String> getArtistId() {
        return artistId;
    }

    @NonNull
    public Optional<Artist> getInitialArtist() {
        return initialArtist;
    }

    public boolean isSaving() {
        return saving;
    }

    public boolean isSavingError() {
        return savingError;
    }

    public boolean isNotUniqueNameError() {
        return notUniqueNameError;
    }

    public boolean isFillError() {
        return fillError;
    }

    public boolean isEmptyNameError() {
        return emptyNameError;
    }

    public boolean isShouldClose() {
        return shouldClose;
    }

    public boolean isShouldAskToDiscard() {
        return shouldAskToDiscard;
    }

    @NonNull
    public Optional<Integer> getDiscardMsg() {
        return discardMsg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EditArtistUiModel that = (EditArtistUiModel) o;

        if (artistIsNew != that.artistIsNew) return false;
        if (loading != that.loading) return false;
        if (loadingError != that.loadingError) return false;
        if (viewDataIsFilled != that.viewDataIsFilled) return false;
        if (saving != that.saving) return false;
        if (savingError != that.savingError) return false;
        if (fillError != that.fillError) return false;
        if (emptyNameError != that.emptyNameError) return false;
        if (notUniqueNameError != that.notUniqueNameError) return false;
        if (shouldClose != that.shouldClose) return false;
        if (shouldAskToDiscard != that.shouldAskToDiscard) return false;
        if (!artistId.equals(that.artistId)) return false;
        if (!initialArtist.equals(that.initialArtist)) return false;
        return discardMsg.equals(that.discardMsg);

    }

    @Override
    public int hashCode() {
        int result = (artistIsNew ? 1 : 0);
        result = 31 * result + (loading ? 1 : 0);
        result = 31 * result + (loadingError ? 1 : 0);
        result = 31 * result + (viewDataIsFilled ? 1 : 0);
        result = 31 * result + artistId.hashCode();
        result = 31 * result + initialArtist.hashCode();
        result = 31 * result + (saving ? 1 : 0);
        result = 31 * result + (savingError ? 1 : 0);
        result = 31 * result + (fillError ? 1 : 0);
        result = 31 * result + (emptyNameError ? 1 : 0);
        result = 31 * result + (notUniqueNameError ? 1 : 0);
        result = 31 * result + (shouldClose ? 1 : 0);
        result = 31 * result + (shouldAskToDiscard ? 1 : 0);
        result = 31 * result + discardMsg.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "EditArtistUiModel{" +
                "artistIsNew=" + artistIsNew +
                ", loading=" + loading +
                ", loadingError=" + loadingError +
                ", viewDataIsFilled=" + viewDataIsFilled +
                ", artistId=" + artistId +
                ", initialArtist=" + initialArtist +
                ", saving=" + saving +
                ", savingError=" + savingError +
                ", fillError=" + fillError +
                ", emptyNameError=" + emptyNameError +
                ", notUniqueNameError=" + notUniqueNameError +
                ", shouldClose=" + shouldClose +
                ", shouldAskToDiscard=" + shouldAskToDiscard +
                ", discardMsg=" + discardMsg +
                '}';
    }

    public static class Builder {
        private boolean artistIsNew;
        private boolean loading;
        private boolean loadingError;
        private boolean viewDataIsFilled;
        @NonNull
        private Optional<String> artistId;
        @NonNull
        private Optional<Artist> initialArtist;
        private boolean saving;
        private boolean savingError;
        private boolean fillError;
        private boolean emptyNameError;
        private boolean notUniqueNameError;
        private boolean shouldClose;
        private boolean shouldAskToDiscard;
        @NonNull
        private Optional<Integer> discardMsg;

        public Builder(EditArtistUiModel model) {
            this.artistIsNew = model.artistIsNew;
            this.loading = model.loading;
            this.loadingError = model.loadingError;
            this.viewDataIsFilled = model.viewDataIsFilled;
            this.artistId = model.artistId;
            this.initialArtist = model.initialArtist;
            this.saving = model.saving;
            this.savingError = model.savingError;
            this.fillError = model.fillError;
            this.emptyNameError = model.emptyNameError;
            this.notUniqueNameError = model.notUniqueNameError;
            this.shouldClose = model.shouldClose;
            this.shouldAskToDiscard = model.shouldAskToDiscard;
            this.discardMsg = model.discardMsg;
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder() {
            artistId = Optional.empty();
            initialArtist = Optional.empty();
        }

        public EditArtistUiModel build() {
            return new EditArtistUiModel(this);
        }

        public Builder artistIsNew(boolean artistIsNew) {
            this.artistIsNew = artistIsNew;
            return this;
        }

        public Builder loading(boolean loading) {
            this.loading = loading;
            return this;
        }

        public Builder loadingError(boolean loadingError) {
            this.loadingError = loadingError;
            return this;
        }

        public Builder viewDataIsFilled(boolean viewDataIsFilled) {
            this.viewDataIsFilled = viewDataIsFilled;
            return this;
        }

        public Builder artistId(@NonNull Optional<String> artistId) {
            this.artistId = artistId;
            return this;
        }

        public Builder initialArtist(@NonNull Optional<Artist> initialArtist) {
            this.initialArtist = initialArtist;
            return this;
        }

        public Builder clearSavingData() {
            this.saving = false;
            this.savingError = false;
            this.fillError = false;
            this.emptyNameError = false;
            this.notUniqueNameError = false;
            this.shouldClose = false;

            return this;
        }

        public Builder saving(boolean saving) {
            this.saving = saving;
            return this;
        }

        public Builder savingError(boolean savingError) {
            this.savingError = savingError;
            return this;
        }

        public Builder fillError(boolean fillError) {
            this.fillError = fillError;
            return this;
        }

        public Builder emptyNameError(boolean emptyNameError) {
            this.emptyNameError = emptyNameError;
            return this;
        }

        public Builder notUniqueNameError(boolean notUniqueNameError) {
            this.notUniqueNameError = notUniqueNameError;
            return this;
        }

        public Builder shouldClose(boolean shouldClose) {
            this.shouldClose = shouldClose;
            return this;
        }

        public Builder shouldAskToDiscard(boolean shouldAskToDiscard) {
            this.shouldAskToDiscard = shouldAskToDiscard;
            return this;
        }

        public Builder discardMsg(@NonNull Optional<Integer> discardMsg) {
            this.discardMsg = discardMsg;
            return this;
        }

        public Builder clearDiscardData() {
            this.shouldClose = false;
            this.shouldAskToDiscard = false;
            this.discardMsg = Optional.empty();

            return this;
        }
    }
}
