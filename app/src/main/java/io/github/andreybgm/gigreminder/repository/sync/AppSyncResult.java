package io.github.andreybgm.gigreminder.repository.sync;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import io.github.andreybgm.gigreminder.data.Concert;

public class AppSyncResult {
    @NonNull
    private final List<Concert> newConcerts;

    public AppSyncResult() {
        this.newConcerts = new ArrayList<>();
    }

    public AppSyncResult(@NonNull List<Concert> newConcerts) {
        this.newConcerts = newConcerts;
    }

    @NonNull
    public List<Concert> getNewConcerts() {
        return newConcerts;
    }

    public AppSyncResult mergeWith(AppSyncResult item) {
        newConcerts.addAll(item.getNewConcerts());

        return this;
    }
}
