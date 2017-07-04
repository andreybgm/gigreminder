package io.github.andreybgm.gigreminder.screen.locationchoice.uievent;

import io.github.andreybgm.gigreminder.screen.base.event.ListItemClickEvent;

public class LocationClickEvent extends ListItemClickEvent {

    public static LocationClickEvent create(int position) {
        return new LocationClickEvent(position);
    }

    private LocationClickEvent(int position) {
        super(position);
    }
}
