package io.github.andreybgm.gigreminder.screen.artistimport.uievent;

import android.support.annotation.NonNull;

import io.github.andreybgm.gigreminder.screen.artistimport.ArtistImportUiModel;
import io.github.andreybgm.gigreminder.screen.base.UiEvent;

public class SaveArtistsEvent implements UiEvent {
    @NonNull
    private final ArtistImportUiModel uiModel;

    public static SaveArtistsEvent create(@NonNull ArtistImportUiModel uiModel) {
        return new SaveArtistsEvent(uiModel);
    }

    private SaveArtistsEvent(@NonNull ArtistImportUiModel uiModel) {
        this.uiModel = uiModel;
    }

    @NonNull
    public ArtistImportUiModel getUiModel() {
        return uiModel;
    }
}
