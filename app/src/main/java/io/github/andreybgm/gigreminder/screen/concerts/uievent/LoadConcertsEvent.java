package io.github.andreybgm.gigreminder.screen.concerts.uievent;

import io.github.andreybgm.gigreminder.screen.base.UiEvent;

public class LoadConcertsEvent implements UiEvent {
    public static final LoadConcertsEvent INSTANCE = new LoadConcertsEvent();

    private LoadConcertsEvent() {
    }
}
