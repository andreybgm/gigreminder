package io.github.andreybgm.gigreminder.screen.base.event;

import io.github.andreybgm.gigreminder.screen.base.Result;
import io.github.andreybgm.gigreminder.screen.base.UiEvent;

public class ListItemClickEvent implements UiEvent, Result {
    private int position;

    public ListItemClickEvent(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
