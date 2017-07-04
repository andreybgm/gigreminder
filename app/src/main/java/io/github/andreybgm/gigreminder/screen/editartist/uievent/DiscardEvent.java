package io.github.andreybgm.gigreminder.screen.editartist.uievent;

import android.support.annotation.NonNull;

import io.github.andreybgm.gigreminder.screen.base.Result;
import io.github.andreybgm.gigreminder.screen.base.UiEvent;

public class DiscardEvent implements UiEvent, Result {
    @NonNull
    private final String name;
    private final boolean forceClose;

    public static DiscardEvent create(@NonNull String name, boolean forceClose) {
        return new DiscardEvent(name, forceClose);
    }

    private DiscardEvent(@NonNull String name, boolean forceClose) {
        this.name = name;
        this.forceClose = forceClose;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public boolean isForceClose() {
        return forceClose;
    }
}
