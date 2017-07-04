package io.github.andreybgm.gigreminder.screen.locations.uievent;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.List;

import io.github.andreybgm.gigreminder.data.Location;
import io.github.andreybgm.gigreminder.screen.base.event.BooleanResult;

public class LoadLocationsResult extends BooleanResult {
    public static final LoadLocationsResult IN_PROGRESS =
            new LoadLocationsResult(State.IN_PROGRESS, null, Collections.emptyList());

    public static LoadLocationsResult error(@NonNull Throwable error) {
        return new LoadLocationsResult(State.ERROR, error, Collections.emptyList());
    }

    public static LoadLocationsResult success(@NonNull List<Location> locations) {
        return new LoadLocationsResult(State.SUCCESS, null, locations);
    }

    @NonNull
    private final List<Location> locations;

    private LoadLocationsResult(@NonNull State state, @Nullable Throwable error,
                                @NonNull List<Location> locations) {
        super(state, error);
        this.locations = locations;
    }

    @NonNull
    public List<Location> getLocations() {
        return locations;
    }
}
