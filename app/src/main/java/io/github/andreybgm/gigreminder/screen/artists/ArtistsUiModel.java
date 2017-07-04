package io.github.andreybgm.gigreminder.screen.artists;

import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import io.github.andreybgm.gigreminder.data.Artist;
import io.github.andreybgm.gigreminder.screen.base.UiModel;
import io.github.andreybgm.gigreminder.utils.Optional;
import io.reactivex.Observable;

public class ArtistsUiModel implements UiModel {
    public static final ArtistsUiModel DEFAULT = ArtistsUiModel.Builder.create().build();

    private final boolean loading;
    private final boolean loadingError;
    @NonNull
    private final List<Artist> artists;
    @NonNull
    private final Optional<Artist> artistToOpen;
    private final boolean actionMode;
    @NonNull
    private final Set<Integer> selectedArtistPositions;
    private final boolean deletion;
    private final boolean deletionError;
    private final boolean shouldOpenArtistsImport;
    private final boolean shouldOpenNewArtist;
    private final boolean actionModeIsExpected;
    @NonNull
    private final List<String> initialSelectedArtistIds;

    public ArtistsUiModel(Builder builder) {
        this.loading = builder.loading;
        this.loadingError = builder.loadingError;
        this.artists = builder.artists;
        this.artistToOpen = builder.artistToOpen;
        this.actionMode = builder.actionMode;
        this.selectedArtistPositions = builder.selectedArtistPositions;
        this.deletion = builder.deletion;
        this.deletionError = builder.deletionError;
        this.shouldOpenArtistsImport = builder.shouldOpenArtistsImport;
        this.shouldOpenNewArtist = builder.shouldOpenNewArtist;
        this.actionModeIsExpected = builder.actionModeIsExpected;
        this.initialSelectedArtistIds = builder.initialSelectedArtistIds;
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

    @NonNull
    public List<Artist> getArtists() {
        return artists;
    }

    @NonNull
    public Optional<Artist> getArtistToOpen() {
        return artistToOpen;
    }

    public boolean isActionModeOn() {
        return actionMode;
    }

    public boolean isArtistSelected(int position) {
        return selectedArtistPositions.contains(position);
    }

    public List<Artist> getSelectedArtists() {
        return Observable.fromIterable(selectedArtistPositions)
                .map(artists::get)
                .toList()
                .blockingGet();
    }

    @NonNull
    public List<String> getSelectedArtistIds() {
        return Observable.fromIterable(selectedArtistPositions)
                .map(artists::get)
                .map(Artist::getId)
                .toList()
                .blockingGet();
    }

    @NonNull
    public Set<Integer> getSelectedArtistPositions() {
        return selectedArtistPositions;
    }

    public boolean isShouldOpenArtistsImport() {
        return shouldOpenArtistsImport;
    }

    public boolean isShouldOpenNewArtist() {
        return shouldOpenNewArtist;
    }

    public boolean isActionModeExpected() {
        return actionModeIsExpected;
    }

    @NonNull
    public List<String> getInitialSelectedArtistIds() {
        return initialSelectedArtistIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArtistsUiModel that = (ArtistsUiModel) o;

        if (loading != that.loading) return false;
        if (loadingError != that.loadingError) return false;
        if (actionMode != that.actionMode) return false;
        if (deletion != that.deletion) return false;
        if (deletionError != that.deletionError) return false;
        if (shouldOpenArtistsImport != that.shouldOpenArtistsImport) return false;
        if (shouldOpenNewArtist != that.shouldOpenNewArtist) return false;
        if (actionModeIsExpected != that.actionModeIsExpected) return false;
        if (!artists.equals(that.artists)) return false;
        if (!artistToOpen.equals(that.artistToOpen)) return false;
        if (!selectedArtistPositions.equals(that.selectedArtistPositions)) return false;
        return initialSelectedArtistIds.equals(that.initialSelectedArtistIds);

    }

    @Override
    public int hashCode() {
        int result = (loading ? 1 : 0);
        result = 31 * result + (loadingError ? 1 : 0);
        result = 31 * result + artists.hashCode();
        result = 31 * result + artistToOpen.hashCode();
        result = 31 * result + (actionMode ? 1 : 0);
        result = 31 * result + selectedArtistPositions.hashCode();
        result = 31 * result + (deletion ? 1 : 0);
        result = 31 * result + (deletionError ? 1 : 0);
        result = 31 * result + (shouldOpenArtistsImport ? 1 : 0);
        result = 31 * result + (shouldOpenNewArtist ? 1 : 0);
        result = 31 * result + (actionModeIsExpected ? 1 : 0);
        result = 31 * result + initialSelectedArtistIds.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ArtistsUiModel{" +
                "loading=" + loading +
                ", loadingError=" + loadingError +
                ", artists=" + artists +
                ", artistToOpen=" + artistToOpen +
                ", actionMode=" + actionMode +
                ", selectedArtistPositions=" + selectedArtistPositions +
                ", deletion=" + deletion +
                ", deletionError=" + deletionError +
                ", shouldOpenArtistsImport=" + shouldOpenArtistsImport +
                ", shouldOpenNewArtist=" + shouldOpenNewArtist +
                ", actionModeIsExpected=" + actionModeIsExpected +
                ", initialSelectedArtistIds=" + initialSelectedArtistIds +
                '}';
    }

    public static class Builder {
        private boolean loading;
        private boolean loadingError;
        @NonNull
        private List<Artist> artists;
        @NonNull
        private Optional<Artist> artistToOpen;
        private boolean actionMode;
        private Set<Integer> selectedArtistPositions;
        private boolean deletion;
        private boolean deletionError;
        private boolean shouldOpenArtistsImport;
        private boolean shouldOpenNewArtist;
        private boolean actionModeIsExpected;
        @NonNull
        private List<String> initialSelectedArtistIds;

        public static Builder create() {
            return new Builder();
        }

        public Builder() {
            artists = Collections.emptyList();
            artistToOpen = Optional.empty();
            selectedArtistPositions = Collections.emptySet();
            initialSelectedArtistIds = Collections.emptyList();
        }

        public Builder(ArtistsUiModel model) {
            this.loading = model.loading;
            this.loadingError = model.loadingError;
            this.artists = model.artists;
            this.artistToOpen = model.artistToOpen;
            this.actionMode = model.actionMode;
            this.selectedArtistPositions = model.selectedArtistPositions;
            this.deletion = model.deletion;
            this.deletionError = model.deletionError;
            this.shouldOpenArtistsImport = model.shouldOpenArtistsImport;
            this.shouldOpenNewArtist = model.shouldOpenNewArtist;
            this.actionModeIsExpected = model.actionModeIsExpected;
            this.initialSelectedArtistIds = model.initialSelectedArtistIds;
        }

        public Builder loading(boolean loading) {
            this.loading = loading;
            return this;
        }

        public Builder loadingError(boolean loadingError) {
            this.loadingError = loadingError;
            return this;
        }

        public Builder artists(@NonNull List<Artist> artists) {
            this.artists = artists;
            return this;
        }

        public Builder openArtist(@NonNull Artist artist) {
            this.artistToOpen = Optional.of(artist);
            return this;
        }

        public Builder clearArtistToOpen() {
            this.artistToOpen = Optional.empty();
            return this;
        }

        public Builder actionMode(boolean actionMode) {
            this.actionMode = actionMode;
            return this;
        }

        public Builder selectedArtistPositions(Set<Integer> selectedArtistPositions) {
            this.selectedArtistPositions = selectedArtistPositions;
            return this;
        }

        public Builder deletion(boolean deletion) {
            this.deletion = deletion;
            return this;
        }

        public Builder deletionError(boolean deletionError) {
            this.deletionError = deletionError;
            return this;
        }

        public Builder turnOffActionMode() {
            this.actionMode = false;
            this.selectedArtistPositions = Collections.emptySet();
            return this;
        }

        public Builder shouldOpenArtistsImport(boolean shouldOpenArtistsImport) {
            this.shouldOpenArtistsImport = shouldOpenArtistsImport;
            return this;
        }

        public Builder shouldOpenNewArtist(boolean shouldOpenNewArtist) {
            this.shouldOpenNewArtist = shouldOpenNewArtist;
            return this;
        }

        public Builder actionModeIsExpected(boolean actionModeIsExpected) {
            this.actionModeIsExpected = actionModeIsExpected;
            return this;
        }

        public Builder initialSelectedArtistIds(List<String> initialSelectedArtistIds) {
            this.initialSelectedArtistIds = initialSelectedArtistIds;
            return this;
        }

        public ArtistsUiModel build() {
            return new ArtistsUiModel(this);
        }
    }
}
