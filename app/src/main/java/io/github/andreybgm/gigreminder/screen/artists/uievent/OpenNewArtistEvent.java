package io.github.andreybgm.gigreminder.screen.artists.uievent;

import io.github.andreybgm.gigreminder.screen.base.event.ConfirmableUiEvent;

public class OpenNewArtistEvent extends ConfirmableUiEvent {
    public static final OpenNewArtistEvent INSTANCE =
            new OpenNewArtistEvent(false);
    public static final OpenNewArtistEvent CONFIRMATION =
            new OpenNewArtistEvent(true);

    private OpenNewArtistEvent(boolean confirmation) {
        super(confirmation);
    }
}
