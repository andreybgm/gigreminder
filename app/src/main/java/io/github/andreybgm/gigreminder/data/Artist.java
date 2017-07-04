package io.github.andreybgm.gigreminder.data;

import android.support.annotation.NonNull;

import java.util.UUID;

public class Artist {

    @NonNull
    private final String id;

    @NonNull
    private final String name;

    public Artist(String name) {
        this(UUID.randomUUID().toString(), name);
    }

    public Artist(@NonNull String id, @NonNull String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Artist{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Artist artist = (Artist) o;

        if (!id.equals(artist.id)) return false;
        return name.equals(artist.name);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
