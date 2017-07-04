package io.github.andreybgm.gigreminder.screen.locations.uievent;

import io.github.andreybgm.gigreminder.screen.base.UiEvent;

public class LoadLocationsEvent implements UiEvent {
    public static final LoadLocationsEvent INSTANCE = new LoadLocationsEvent();

    private LoadLocationsEvent() {
    }
}
