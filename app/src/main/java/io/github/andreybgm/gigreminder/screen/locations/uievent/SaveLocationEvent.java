package io.github.andreybgm.gigreminder.screen.locations.uievent;

import android.support.annotation.NonNull;

import io.github.andreybgm.gigreminder.data.Location;
import io.github.andreybgm.gigreminder.screen.base.UiEvent;

public class SaveLocationEvent implements UiEvent {
    @NonNull
    private final Location location;

    public static SaveLocationEvent create(@NonNull Location location) {
        return new SaveLocationEvent(location);
    }

    private SaveLocationEvent(@NonNull Location location) {
        this.location = location;
    }

    @NonNull
    public Location getLocation() {
        return location;
    }
}
