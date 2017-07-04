package io.github.andreybgm.gigreminder.screen.editartist.uievent;

import android.support.annotation.NonNull;

import io.github.andreybgm.gigreminder.screen.base.UiEvent;
import io.github.andreybgm.gigreminder.screen.editartist.EditArtistUiModel;

public class SaveArtistEvent implements UiEvent {
    @NonNull
    private final EditArtistUiModel uiModel;
    @NonNull
    private final String name;

    public static SaveArtistEvent create(@NonNull EditArtistUiModel uiModel,
                                         @NonNull String name) {
        return new SaveArtistEvent(uiModel, name);
    }

    private SaveArtistEvent(@NonNull EditArtistUiModel uiModel, @NonNull String name) {
        this.uiModel = uiModel;
        this.name = name;
    }

    @NonNull
    public EditArtistUiModel getUiModel() {
        return uiModel;
    }

    @NonNull
    public String getName() {
        return name;
    }
}
