package io.github.andreybgm.gigreminder.screen.concertdetails.uievent;

import io.github.andreybgm.gigreminder.screen.base.event.ConfirmableUiEvent;

public class SiteClickEvent extends ConfirmableUiEvent {
    public static final SiteClickEvent INSTANCE =
            new SiteClickEvent(false);
    public static final SiteClickEvent CONFIRMATION =
            new SiteClickEvent(true);

    private SiteClickEvent(boolean confirmation) {
        super(confirmation);
    }
}
