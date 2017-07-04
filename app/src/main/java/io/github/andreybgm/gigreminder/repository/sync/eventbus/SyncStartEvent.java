package io.github.andreybgm.gigreminder.repository.sync.eventbus;

import io.github.andreybgm.gigreminder.eventbus.BaseEvent;

public class SyncStartEvent extends BaseEvent {
    public static SyncStartEvent create() {
        return new SyncStartEvent();
    }
}
