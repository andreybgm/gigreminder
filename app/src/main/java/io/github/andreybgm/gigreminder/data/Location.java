package io.github.andreybgm.gigreminder.data;

import android.support.annotation.NonNull;

import java.util.UUID;

public class Location {
    @NonNull
    private final String id;

    @NonNull
    private final String apiCode;

    @NonNull
    private final String name;

    public Location(String apiCode, String name) {
        this(UUID.randomUUID().toString(), apiCode, name);
    }

    public Location(@NonNull String id, @NonNull String apiCode, @NonNull String name) {
        this.id = id;
        this.apiCode = apiCode;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getApiCode() {
        return apiCode;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Location{" +
                "id='" + id + '\'' +
                ", apiCode='" + apiCode + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Location location = (Location) o;

        if (!id.equals(location.id)) return false;
        if (!apiCode.equals(location.apiCode)) return false;
        return name.equals(location.name);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + apiCode.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    public boolean equalsIgnoreId(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Location location = (Location) o;

        if (!apiCode.equals(location.apiCode)) return false;
        return name.equals(location.name);
    }
}
