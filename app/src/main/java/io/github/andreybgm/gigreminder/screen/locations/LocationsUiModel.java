package io.github.andreybgm.gigreminder.screen.locations;

import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.List;

import io.github.andreybgm.gigreminder.data.Location;
import io.github.andreybgm.gigreminder.screen.base.UiModel;

public class LocationsUiModel implements UiModel {

    public static LocationsUiModel DEFAULT = Builder.create().build();

    private final boolean loading;
    private final boolean loadingError;
    @NonNull
    private final List<Location> locations;
    private final boolean deletion;
    private final boolean deletionError;
    private final boolean saving;
    private final boolean savingError;
    private final boolean shouldOpenNewLocation;

    public LocationsUiModel(Builder builder) {
        this.loading = builder.loading;
        this.loadingError = builder.loadingError;
        this.locations = builder.locations;
        this.deletion = builder.deletion;
        this.deletionError = builder.deletionError;
        this.saving = builder.saving;
        this.savingError = builder.savingError;
        this.shouldOpenNewLocation = builder.shouldOpenNewLocation;
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
    public List<Location> getLocations() {
        return locations;
    }

    public boolean isDeletion() {
        return deletion;
    }

    public boolean isDeletionError() {
        return deletionError;
    }

    public boolean isSaving() {
        return saving;
    }

    public boolean isSavingError() {
        return savingError;
    }

    public boolean isShouldOpenNewLocation() {
        return shouldOpenNewLocation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocationsUiModel that = (LocationsUiModel) o;

        if (loading != that.loading) return false;
        if (loadingError != that.loadingError) return false;
        if (deletion != that.deletion) return false;
        if (deletionError != that.deletionError) return false;
        if (saving != that.saving) return false;
        if (savingError != that.savingError) return false;
        if (shouldOpenNewLocation != that.shouldOpenNewLocation) return false;
        return locations.equals(that.locations);

    }

    @Override
    public int hashCode() {
        int result = (loading ? 1 : 0);
        result = 31 * result + (loadingError ? 1 : 0);
        result = 31 * result + locations.hashCode();
        result = 31 * result + (deletion ? 1 : 0);
        result = 31 * result + (deletionError ? 1 : 0);
        result = 31 * result + (saving ? 1 : 0);
        result = 31 * result + (savingError ? 1 : 0);
        result = 31 * result + (shouldOpenNewLocation ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LocationsUiModel{" +
                "loading=" + loading +
                ", loadingError=" + loadingError +
                ", locations=" + locations +
                ", deletion=" + deletion +
                ", deletionError=" + deletionError +
                ", saving=" + saving +
                ", savingError=" + savingError +
                ", shouldOpenNewLocation=" + shouldOpenNewLocation +
                '}';
    }

    public static class Builder {
        private boolean loading;
        private boolean loadingError;
        @NonNull
        private List<Location> locations;
        private boolean deletion;
        private boolean deletionError;
        private boolean saving;
        private boolean savingError;
        private boolean shouldOpenNewLocation;

        public static Builder create() {
            return new Builder();
        }

        public Builder() {
            locations = Collections.emptyList();
        }

        public Builder(LocationsUiModel model) {
            this.loading = model.loading;
            this.loadingError = model.loadingError;
            this.locations = model.locations;
            this.deletion = model.deletion;
            this.deletionError = model.deletionError;
            this.saving = model.saving;
            this.savingError = model.savingError;
            this.shouldOpenNewLocation = model.shouldOpenNewLocation;
        }

        public LocationsUiModel build() {
            return new LocationsUiModel(this);
        }

        public Builder loading(boolean loading) {
            this.loading = loading;
            return this;
        }

        public Builder loadingError(boolean loadingError) {
            this.loadingError = loadingError;
            return this;
        }

        public Builder locations(@NonNull List<Location> locations) {
            this.locations = locations;
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

        public Builder saving(boolean saving) {
            this.saving = saving;
            return this;
        }

        public Builder savingError(boolean savingError) {
            this.savingError = savingError;
            return this;
        }

        public Builder shouldOpenNewLocation(boolean shouldOpenNewLocation) {
            this.shouldOpenNewLocation = shouldOpenNewLocation;
            return this;
        }
    }
}
