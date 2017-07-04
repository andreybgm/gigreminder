package io.github.andreybgm.gigreminder.screen.artistimport.uievent;

import io.github.andreybgm.gigreminder.screen.base.event.ListItemClickEvent;

public class ArtistClickEvent extends ListItemClickEvent {

    public static ArtistClickEvent create(int position) {
        return new ArtistClickEvent(position);
    }

    private ArtistClickEvent(int position) {
        super(position);
    }
}
