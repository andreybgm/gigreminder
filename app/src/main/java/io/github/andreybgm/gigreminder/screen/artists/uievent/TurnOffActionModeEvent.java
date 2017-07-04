package io.github.andreybgm.gigreminder.screen.artists.uievent;

import io.github.andreybgm.gigreminder.screen.base.Result;
import io.github.andreybgm.gigreminder.screen.base.UiEvent;

public class TurnOffActionModeEvent implements UiEvent, Result {
    public static final TurnOffActionModeEvent INSTANCE = new TurnOffActionModeEvent();

    private TurnOffActionModeEvent() {
    }
}
