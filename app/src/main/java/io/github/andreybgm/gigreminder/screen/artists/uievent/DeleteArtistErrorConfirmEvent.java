package io.github.andreybgm.gigreminder.screen.artists.uievent;

import io.github.andreybgm.gigreminder.screen.base.Result;
import io.github.andreybgm.gigreminder.screen.base.UiEvent;


public class DeleteArtistErrorConfirmEvent implements UiEvent, Result {
    public static DeleteArtistErrorConfirmEvent INSTANCE =
            new DeleteArtistErrorConfirmEvent();

    private DeleteArtistErrorConfirmEvent() {
    }
}
