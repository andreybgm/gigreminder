package io.github.andreybgm.gigreminder.screen.locations.uievent;

import io.github.andreybgm.gigreminder.screen.base.Result;
import io.github.andreybgm.gigreminder.screen.base.UiEvent;

public class DeleteLocationErrorConfirmEvent implements UiEvent, Result {
    public static DeleteLocationErrorConfirmEvent INSTANCE =
            new DeleteLocationErrorConfirmEvent();

    private DeleteLocationErrorConfirmEvent() {
    }
}
