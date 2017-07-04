package io.github.andreybgm.gigreminder.screen.concerts.uievent;

import io.github.andreybgm.gigreminder.screen.base.event.ListItemClickEvent;

public class ConcertClickEvent extends ListItemClickEvent {
    public static ConcertClickEvent create(int position) {
        return new ConcertClickEvent(position);
    }

    private ConcertClickEvent(int position) {
        super(position);
    }
}
