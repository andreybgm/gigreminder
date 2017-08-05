package io.github.andreybgm.gigreminder.repository;

import java.util.Date;
import java.util.List;

import io.github.andreybgm.gigreminder.data.Artist;
import io.github.andreybgm.gigreminder.data.Concert;
import io.github.andreybgm.gigreminder.data.Location;
import io.github.andreybgm.gigreminder.data.SyncState;
import io.github.andreybgm.gigreminder.repository.sync.AppSyncResult;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface DataSource {
    Observable<List<Artist>> getArtists();

    Single<Artist> getArtist(String id);

    Completable saveArtist(Artist artist);

    Completable saveArtists(List<Artist> artists);

    Completable updateArtist(Artist artist);

    Completable deleteArtist(Artist artist);

    Completable deleteArtists(List<Artist> artists);

    Single<List<String>> loadArtistsFromGoogleMusic();

    Observable<List<Location>> getLocations();

    Single<Location> getLocation(String id);

    Completable saveLocation(Location location);

    Completable saveLocations(List<Location> locations);

    Completable deleteLocation(Location location);

    Single<List<Location>> getAvailableLocations();

    Observable<List<Concert>> getConcerts();

    Single<Concert> getConcert(String id);

    Completable saveConcert(Concert concert);

    Completable saveConcerts(List<Concert> concert);

    Completable deleteConcert(Concert concert);

    Single<List<SyncState>> getSyncStates();

    Single<List<SyncState>> getSyncStatesToUpdate(Date currentTime, long relevancePeriodHours);

    Single<Boolean> isSyncRequired(Date currentTime, long relevancePeriodHours);

    Completable saveSyncState(SyncState syncState);

    Single<AppSyncResult> syncData(Date currentTime, long relevancePeriodHours);

    void onSyncInterrupted();

    void close();
}
