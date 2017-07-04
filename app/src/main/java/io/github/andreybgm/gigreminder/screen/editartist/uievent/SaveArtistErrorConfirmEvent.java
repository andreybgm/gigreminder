package io.github.andreybgm.gigreminder.screen.editartist.uievent;

import io.github.andreybgm.gigreminder.screen.base.Result;
import io.github.andreybgm.gigreminder.screen.base.UiEvent;

public class SaveArtistErrorConfirmEvent implements UiEvent, Result {
    public static SaveArtistErrorConfirmEvent INSTANCE =
            new SaveArtistErrorConfirmEvent();

    private SaveArtistErrorConfirmEvent() {
    }
}
