package io.github.andreybgm.gigreminder.screen.artists.uievent;

import io.github.andreybgm.gigreminder.screen.base.event.ListItemClickEvent;

public class ArtistLongClickEvent extends ListItemClickEvent {

    public static ArtistLongClickEvent create(int position) {
        return new ArtistLongClickEvent(position);
    }

    private ArtistLongClickEvent(int position) {
        super(position);
    }
}
