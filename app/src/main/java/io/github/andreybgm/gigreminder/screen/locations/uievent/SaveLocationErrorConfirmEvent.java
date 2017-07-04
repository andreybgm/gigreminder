package io.github.andreybgm.gigreminder.screen.locations.uievent;

import io.github.andreybgm.gigreminder.screen.base.Result;
import io.github.andreybgm.gigreminder.screen.base.UiEvent;

public class SaveLocationErrorConfirmEvent implements UiEvent, Result {
    public static SaveLocationErrorConfirmEvent INSTANCE =
            new SaveLocationErrorConfirmEvent();

    private SaveLocationErrorConfirmEvent() {
    }
}
