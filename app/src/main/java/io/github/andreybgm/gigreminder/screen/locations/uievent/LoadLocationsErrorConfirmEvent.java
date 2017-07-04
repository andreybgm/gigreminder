package io.github.andreybgm.gigreminder.screen.locations.uievent;

import io.github.andreybgm.gigreminder.screen.base.Result;
import io.github.andreybgm.gigreminder.screen.base.UiEvent;

public class LoadLocationsErrorConfirmEvent implements UiEvent, Result {
    public static final LoadLocationsErrorConfirmEvent INSTANCE =
            new LoadLocationsErrorConfirmEvent();

    private LoadLocationsErrorConfirmEvent() {
    }
}
