package io.github.andreybgm.gigreminder.screen.locations.uievent;

import android.support.annotation.NonNull;

import io.github.andreybgm.gigreminder.screen.base.UiEvent;
import io.github.andreybgm.gigreminder.screen.locations.LocationsUiModel;

public class DeleteLocationEvent implements UiEvent {
    private final int position;
    @NonNull
    private final LocationsUiModel uiModel;

    public static DeleteLocationEvent create(int position, @NonNull LocationsUiModel uiModel) {
        return new DeleteLocationEvent(position, uiModel);
    }

    private DeleteLocationEvent(int position, @NonNull LocationsUiModel uiModel) {
        this.position = position;
        this.uiModel = uiModel;
    }

    public int getPosition() {
        return position;
    }

    @NonNull
    public LocationsUiModel getUiModel() {
        return uiModel;
    }
}
