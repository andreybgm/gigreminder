package io.github.andreybgm.gigreminder.screen.editartist.uievent;

import io.github.andreybgm.gigreminder.screen.base.Result;
import io.github.andreybgm.gigreminder.screen.base.UiEvent;

public class DiscardConfirmEvent implements UiEvent, Result {
    public static DiscardConfirmEvent INSTANCE = new DiscardConfirmEvent();

    private DiscardConfirmEvent() {
    }
}
