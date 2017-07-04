package io.github.andreybgm.gigreminder.screen.concerts.uievent;

import io.github.andreybgm.gigreminder.screen.base.Result;
import io.github.andreybgm.gigreminder.screen.base.UiEvent;

public class OpenConcertConfirmEvent implements UiEvent, Result {
    public static final OpenConcertConfirmEvent INSTANCE = new OpenConcertConfirmEvent();

    private OpenConcertConfirmEvent() {
    }
}
