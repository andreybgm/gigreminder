package io.github.andreybgm.gigreminder.screen.artists.uievent;

import io.github.andreybgm.gigreminder.screen.base.Result;
import io.github.andreybgm.gigreminder.screen.base.UiEvent;

public class OpenArtistConfirmEvent implements UiEvent, Result {
    public static final OpenArtistConfirmEvent INSTANCE = new OpenArtistConfirmEvent();

    private OpenArtistConfirmEvent() {
    }
}
