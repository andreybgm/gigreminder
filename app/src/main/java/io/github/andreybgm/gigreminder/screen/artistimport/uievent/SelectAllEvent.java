package io.github.andreybgm.gigreminder.screen.artistimport.uievent;

import io.github.andreybgm.gigreminder.screen.base.event.ConfirmableUiEvent;

public class SelectAllEvent extends ConfirmableUiEvent {
    public static final SelectAllEvent INSTANCE =
            new SelectAllEvent(false);
    public static final SelectAllEvent CONFIRMATION =
            new SelectAllEvent(true);

    private SelectAllEvent(boolean confirmation) {
        super(confirmation);
    }
}
