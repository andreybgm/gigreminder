package io.github.andreybgm.gigreminder.screen.locations.uievent;

import io.github.andreybgm.gigreminder.screen.base.event.ConfirmableUiEvent;

public class OpenNewLocationEvent extends ConfirmableUiEvent {
    public static final OpenNewLocationEvent INSTANCE =
            new OpenNewLocationEvent(false);
    public static final OpenNewLocationEvent CONFIRMATION =
            new OpenNewLocationEvent(true);

    private OpenNewLocationEvent(boolean confirmation) {
        super(confirmation);
    }
}
