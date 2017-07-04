package io.github.andreybgm.gigreminder.screen.locationchoice;

import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.List;

import io.github.andreybgm.gigreminder.data.Location;
import io.github.andreybgm.gigreminder.screen.base.UiModel;
import io.github.andreybgm.gigreminder.utils.Optional;

public class LocationChoiceUiModel implements UiModel {

    public static LocationChoiceUiModel DEFAULT = Builder.create().build();

    private final boolean loading;
    private final boolean loadingError;
    @NonNull
    private final List<Location> locations;
    @NonNull
    private final Optional<Location> locationToOpen;

    public LocationChoiceUiModel(Builder builder) {
        this.loading = builder.loading;
        this.loadingError = builder.loadingError;
        this.locations = builder.locations;
        this.locationToOpen = builder.locationToOpen;
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

    @NonNull
    public Optional<Location> getLocationToOpen() {
        return locationToOpen;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocationChoiceUiModel that = (LocationChoiceUiModel) o;

        if (loading != that.loading) return false;
        if (loadingError != that.loadingError) return false;
        if (!locations.equals(that.locations)) return false;
        return locationToOpen.equals(that.locationToOpen);

    }

    @Override
    public int hashCode() {
        int result = (loading ? 1 : 0);
        result = 31 * result + (loadingError ? 1 : 0);
        result = 31 * result + locations.hashCode();
        result = 31 * result + locationToOpen.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "LocationChoiceUiModel{" +
                "loading=" + loading +
                ", loadingError=" + loadingError +
                ", locations=" + locations +
                ", locationToOpen=" + locationToOpen +
                '}';
    }

    public static class Builder {
        private boolean loading;
        private boolean loadingError;
        @NonNull
        private List<Location> locations;
        @NonNull
        private Optional<Location> locationToOpen;

        public static Builder create() {
            return new Builder();
        }

        public Builder() {
            locations = Collections.emptyList();
            locationToOpen = Optional.empty();
        }

        public Builder(LocationChoiceUiModel model) {
            this.loading = model.loading;
            this.loadingError = model.loadingError;
            this.locations = model.locations;
            this.locationToOpen = model.locationToOpen;
        }

        public LocationChoiceUiModel build() {
            return new LocationChoiceUiModel(this);
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

        public Builder locationToOpen(@NonNull Location location) {
            this.locationToOpen = Optional.of(location);
            return this;
        }

        public Builder clearLocationToOpen() {
            this.locationToOpen = Optional.empty();
            return this;
        }
    }
}
