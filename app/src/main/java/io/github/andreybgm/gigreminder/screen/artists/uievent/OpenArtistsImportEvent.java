package io.github.andreybgm.gigreminder.screen.artists.uievent;

import io.github.andreybgm.gigreminder.screen.base.event.ConfirmableUiEvent;

public class OpenArtistsImportEvent extends ConfirmableUiEvent {
    public static final OpenArtistsImportEvent INSTANCE =
            new OpenArtistsImportEvent(false);
    public static final OpenArtistsImportEvent CONFIRMATION =
            new OpenArtistsImportEvent(true);

    private OpenArtistsImportEvent(boolean confirmation) {
        super(confirmation);
    }
}
