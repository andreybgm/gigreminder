package io.github.andreybgm.gigreminder.screen.base.event;

import io.github.andreybgm.gigreminder.screen.base.Result;
import io.github.andreybgm.gigreminder.screen.base.UiEvent;

public class ConfirmableUiEvent implements UiEvent, Result {
    private final boolean confirmation;

    public ConfirmableUiEvent(boolean confirmation) {
        this.confirmation = confirmation;
    }

    public boolean isConfirmation() {
        return confirmation;
    }
}
