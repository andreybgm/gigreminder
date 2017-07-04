package io.github.andreybgm.gigreminder.screen.artistimport.uievent;

import io.github.andreybgm.gigreminder.screen.base.event.ConfirmableUiEvent;

public class UnselectAllEvent extends ConfirmableUiEvent {
    public static final UnselectAllEvent INSTANCE =
            new UnselectAllEvent(false);
    public static final UnselectAllEvent CONFIRMATION =
            new UnselectAllEvent(true);

    private UnselectAllEvent(boolean confirmation) {
        super(confirmation);
    }
}
