package io.github.andreybgm.gigreminder.repository.entityrepository;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import io.github.andreybgm.gigreminder.data.Concert;

public class SyncResult {
    @NonNull
    private final List<Concert> newConcerts;

    SyncResult() {
        this.newConcerts = new ArrayList<>();
    }

    SyncResult(@NonNull List<Concert> newConcerts) {
        this.newConcerts = newConcerts;
    }

    @NonNull
    public List<Concert> getNewConcerts() {
        return newConcerts;
    }

    SyncResult mergeWith(SyncResult item) {
        newConcerts.addAll(item.getNewConcerts());

        return this;
    }
}
