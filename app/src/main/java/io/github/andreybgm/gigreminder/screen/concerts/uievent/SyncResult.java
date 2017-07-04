package io.github.andreybgm.gigreminder.screen.concerts.uievent;

import io.github.andreybgm.gigreminder.screen.base.Result;

public class SyncResult implements Result {
    private final boolean syncing;

    public static SyncResult create(boolean syncing) {
        return new SyncResult(syncing);
    }

    private SyncResult(boolean syncing) {
        this.syncing = syncing;
    }

    public boolean isSyncing() {
        return syncing;
    }
}
