package io.github.andreybgm.gigreminder.repository.sync.eventbus;

import io.github.andreybgm.gigreminder.eventbus.BaseEvent;

public class SyncFinishEvent extends BaseEvent {
    public static SyncFinishEvent create() {
        return new SyncFinishEvent();
    }
}
