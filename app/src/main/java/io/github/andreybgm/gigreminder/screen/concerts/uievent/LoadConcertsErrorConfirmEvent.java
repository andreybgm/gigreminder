package io.github.andreybgm.gigreminder.screen.concerts.uievent;

import io.github.andreybgm.gigreminder.screen.base.Result;
import io.github.andreybgm.gigreminder.screen.base.UiEvent;

public class LoadConcertsErrorConfirmEvent implements UiEvent, Result {
    public static final LoadConcertsErrorConfirmEvent INSTANCE =
            new LoadConcertsErrorConfirmEvent();

    private LoadConcertsErrorConfirmEvent() {
    }
}
