package io.github.andreybgm.gigreminder.screen.artists.uievent;

import android.support.annotation.NonNull;

import io.github.andreybgm.gigreminder.screen.artists.ArtistsUiModel;
import io.github.andreybgm.gigreminder.screen.base.UiEvent;

public class DeleteArtistsEvent implements UiEvent {
    @NonNull
    private final ArtistsUiModel uiModel;

    public static DeleteArtistsEvent create(@NonNull ArtistsUiModel uiModel) {
        return new DeleteArtistsEvent(uiModel);
    }

    private DeleteArtistsEvent(@NonNull ArtistsUiModel uiModel) {
        this.uiModel = uiModel;
    }

    @NonNull
    public ArtistsUiModel getUiModel() {
        return uiModel;
    }
}
