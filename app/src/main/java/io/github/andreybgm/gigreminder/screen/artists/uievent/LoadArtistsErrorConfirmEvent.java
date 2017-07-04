package io.github.andreybgm.gigreminder.screen.artists.uievent;

import io.github.andreybgm.gigreminder.screen.base.Result;
import io.github.andreybgm.gigreminder.screen.base.UiEvent;

public class LoadArtistsErrorConfirmEvent implements UiEvent, Result {
    public static final LoadArtistsErrorConfirmEvent INSTANCE = new LoadArtistsErrorConfirmEvent();

    private LoadArtistsErrorConfirmEvent() {
    }
}
