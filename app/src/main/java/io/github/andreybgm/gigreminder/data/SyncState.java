package io.github.andreybgm.gigreminder.data;

import android.support.annotation.NonNull;

import java.util.Date;

public class SyncState {
    @NonNull
    private final Artist artist;

    @NonNull
    private final Location location;

    @NonNull
    private final Date lastSync;

    public SyncState(@NonNull Artist artist, @NonNull Location location) {
        this(artist, location, new Date(0));
    }

    public SyncState(@NonNull Artist artist, @NonNull Location location, @NonNull Date lastSync) {
        this.artist = artist;
        this.location = location;
        this.lastSync = lastSync;
    }

    @NonNull
    public Artist getArtist() {
        return artist;
    }

    @NonNull
    public Location getLocation() {
        return location;
    }

    @NonNull
    public Date getLastSync() {
        return lastSync;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SyncState that = (SyncState) o;

        if (!artist.equals(that.artist)) return false;
        if (!location.equals(that.location)) return false;
        return lastSync.equals(that.lastSync);

    }

    @Override
    public int hashCode() {
        int result = artist.hashCode();
        result = 31 * result + location.hashCode();
        result = 31 * result + lastSync.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SyncState{" +
                "artist=" + artist +
                ", location=" + location +
                ", lastSync=" + lastSync +
                '}';
    }
}
