package io.github.andreybgm.gigreminder.repository.db.entity;

import io.github.andreybgm.gigreminder.repository.DataSource;

public class EntityRegistry {
    public final ArtistEntity artist;
    public final LocationEntity location;
    public final ConcertEntity concert;
    public final SyncStateEntity syncState;

    public EntityRegistry(DataSource repository) {
        artist = new ArtistEntity(repository);
        location = new LocationEntity(repository);
        concert = new ConcertEntity(repository);
        syncState = new SyncStateEntity(repository);
    }
}
