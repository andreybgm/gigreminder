package io.github.andreybgm.gigreminder.screen.artistimport.uievent;

import io.github.andreybgm.gigreminder.screen.base.Result;
import io.github.andreybgm.gigreminder.screen.base.UiEvent;


public class SaveArtistsErrorConfirmEvent implements UiEvent, Result {
    public static SaveArtistsErrorConfirmEvent INSTANCE =
            new SaveArtistsErrorConfirmEvent();

    private SaveArtistsErrorConfirmEvent() {
    }
}
