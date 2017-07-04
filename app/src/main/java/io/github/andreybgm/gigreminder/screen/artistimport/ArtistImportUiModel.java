package io.github.andreybgm.gigreminder.screen.artistimport;

import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import io.github.andreybgm.gigreminder.screen.base.UiModel;
import io.github.andreybgm.gigreminder.utils.Optional;
import io.reactivex.Observable;

public class ArtistImportUiModel implements UiModel {

    public static final ArtistImportUiModel DEFAULT = ArtistImportUiModel.Builder.create()
            .firstLoading(true)
            .build();

    private final boolean loading;
    private final boolean loadingError;
    private final boolean firstLoading;
    @NonNull
    private final List<String> artists;
    @NonNull
    private final Set<Integer> selectedArtistPositions;
    @NonNull
    private final Optional<List<String>> initialSelectedArtistNames;
    private final boolean saving;
    @NonNull
    private final Optional<Integer> savingError;
    private final boolean shouldClose;

    public ArtistImportUiModel(Builder builder) {
        this.loading = builder.loading;
        this.loadingError = builder.loadingError;
        this.firstLoading = builder.firstLoading;
        this.artists = builder.artists;
        this.selectedArtistPositions = builder.selectedArtistPositions;
        this.initialSelectedArtistNames = builder.initialSelectedArtistNames;
        this.saving = builder.saving;
        this.savingError = builder.savingError;
        this.shouldClose = builder.shouldClose;
    }

    public Builder copy() {
        return new Builder(this);
    }

    public boolean isLoading() {
        return loading;
    }

    public boolean isLoadingError() {
        return loadingError;
    }

    public boolean isFirstLoading() {
        return firstLoading;
    }

    @NonNull
    public List<String> getArtists() {
        return artists;
    }

    public boolean isArtistSelected(int position) {
        return selectedArtistPositions.contains(position);
    }

    @NonNull
    public List<String> getSelectedArtists() {
        return Observable.fromIterable(selectedArtistPositions)
                .map(artists::get)
                .toList()
                .blockingGet();
    }

    @NonNull
    public Set<Integer> getSelectedArtistPositions() {
        return selectedArtistPositions;
    }

    @NonNull
    public Optional<List<String>> getInitialSelectedArtistNames() {
        return initialSelectedArtistNames;
    }

    public boolean isSaving() {
        return saving;
    }

    @NonNull
    public Optional<Integer> getSavingError() {
        return savingError;
    }

    public boolean isShouldClose() {
        return shouldClose;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArtistImportUiModel that = (ArtistImportUiModel) o;

        if (loading != that.loading) return false;
        if (loadingError != that.loadingError) return false;
        if (firstLoading != that.firstLoading) return false;
        if (saving != that.saving) return false;
        if (shouldClose != that.shouldClose) return false;
        if (!artists.equals(that.artists)) return false;
        if (!selectedArtistPositions.equals(that.selectedArtistPositions)) return false;
        if (!initialSelectedArtistNames.equals(that.initialSelectedArtistNames)) return false;
        return savingError.equals(that.savingError);

    }

    @Override
    public int hashCode() {
        int result = (loading ? 1 : 0);
        result = 31 * result + (loadingError ? 1 : 0);
        result = 31 * result + (firstLoading ? 1 : 0);
        result = 31 * result + artists.hashCode();
        result = 31 * result + selectedArtistPositions.hashCode();
        result = 31 * result + initialSelectedArtistNames.hashCode();
        result = 31 * result + (saving ? 1 : 0);
        result = 31 * result + savingError.hashCode();
        result = 31 * result + (shouldClose ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ArtistImportUiModel{" +
                "loading=" + loading +
                ", loadingError=" + loadingError +
                ", firstLoading=" + firstLoading +
                ", artists=" + artists +
                ", selectedArtistPositions=" + selectedArtistPositions +
                ", initialSelectedArtistNames=" + initialSelectedArtistNames +
                ", saving=" + saving +
                ", savingError=" + savingError +
                ", shouldClose=" + shouldClose +
                '}';
    }

    public static class Builder {
        private boolean loading;
        private boolean loadingError;
        private boolean firstLoading;
        @NonNull
        private List<String> artists;
        @NonNull
        private Set<Integer> selectedArtistPositions;
        @NonNull
        private Optional<List<String>> initialSelectedArtistNames;
        private boolean saving;
        @NonNull
        private Optional<Integer> savingError;
        private boolean shouldClose;

        public static Builder create() {
            return new Builder();
        }

        public Builder() {
            artists = Collections.emptyList();
            selectedArtistPositions = Collections.emptySet();
            initialSelectedArtistNames = Optional.empty();
            savingError = Optional.empty();
        }

        public Builder(ArtistImportUiModel model) {
            this.loading = model.loading;
            this.loadingError = model.loadingError;
            this.firstLoading = model.firstLoading;
            this.artists = model.artists;
            this.selectedArtistPositions = model.selectedArtistPositions;
            this.initialSelectedArtistNames = model.initialSelectedArtistNames;
            this.saving = model.saving;
            this.savingError = model.savingError;
            this.shouldClose = model.shouldClose;
        }

        public Builder loading(boolean loading) {
            this.loading = loading;
            return this;
        }

        public Builder loadingError(boolean loadingError) {
            this.loadingError = loadingError;
            return this;
        }

        public Builder firstLoading(boolean firstLoading) {
            this.firstLoading = firstLoading;
            return this;
        }

        public Builder artists(@NonNull List<String> artists) {
            this.artists = artists;
            return this;
        }

        public Builder selectedArtistPositions(@NonNull Set<Integer> selectedArtistPositions) {
            this.selectedArtistPositions = selectedArtistPositions;
            return this;
        }

        public Builder initialSelectedArtistNames(@NonNull Optional<List<String>>
                                                          initialSelectedArtistNames) {
            this.initialSelectedArtistNames = initialSelectedArtistNames;
            return this;
        }

        public Builder clearInitialSelectedArtistNames() {
            this.initialSelectedArtistNames = Optional.empty();
            return this;
        }

        public Builder saving(boolean saving) {
            this.saving = saving;
            return this;
        }

        public Builder savingError(Optional<Integer> savingError) {
            this.savingError = savingError;
            return this;
        }

        public Builder shouldClose(boolean shouldClose) {
            this.shouldClose = shouldClose;
            return this;
        }

        public ArtistImportUiModel build() {
            return new ArtistImportUiModel(this);
        }
    }
}
