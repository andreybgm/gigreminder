package io.github.andreybgm.gigreminder.screen.editartist.uievent;

import io.github.andreybgm.gigreminder.screen.base.Result;
import io.github.andreybgm.gigreminder.screen.base.UiEvent;

public class ViewDataIsFilledEvent implements UiEvent, Result {
    public static ViewDataIsFilledEvent INSTANCE =
            new ViewDataIsFilledEvent();

    private ViewDataIsFilledEvent() {
    }
}
