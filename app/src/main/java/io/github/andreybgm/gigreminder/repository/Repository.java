package io.github.andreybgm.gigreminder.repository;

import android.content.Context;

import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;

import java.util.Date;
import java.util.List;

import io.github.andreybgm.gigreminder.BuildConfig;
import io.github.andreybgm.gigreminder.data.Artist;
import io.github.andreybgm.gigreminder.data.Concert;
import io.github.andreybgm.gigreminder.data.Location;
import io.github.andreybgm.gigreminder.data.SyncState;
import io.github.andreybgm.gigreminder.repository.db.BriteDatabaseHelper;
import io.github.andreybgm.gigreminder.repository.db.DbHelper;
import io.github.andreybgm.gigreminder.repository.entityrepository.ArtistRepository;
import io.github.andreybgm.gigreminder.repository.entityrepository.ConcertRepository;
import io.github.andreybgm.gigreminder.repository.entityrepository.Dependencies;
import io.github.andreybgm.gigreminder.repository.entityrepository.LocationRepository;
import io.github.andreybgm.gigreminder.repository.entityrepository.SyncRepository;
import io.github.andreybgm.gigreminder.repository.entityrepository.SyncResult;
import io.github.andreybgm.gigreminder.repository.entityrepository.SyncStateRepository;
import io.github.andreybgm.gigreminder.utils.schedulers.DefaultSchedulerProvider;
import io.github.andreybgm.gigreminder.utils.schedulers.SchedulerProvider;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import rx.schedulers.Schedulers;

public class Repository implements DataSource {

    private final ArtistRepository artistRepository;
    private final LocationRepository locationRepository;
    private final SyncStateRepository syncStateRepository;
    private final ConcertRepository concertRepository;
    private final BriteDatabaseHelper briteDatabaseHelper;
    private final SyncRepository syncRepository;

    public Repository(Context context) {
        SchedulerProvider schedulerProvider = DefaultSchedulerProvider.getInstance();
        DbHelper openHelper = new DbHelper(context);
        SqlBrite sqlBrite = new SqlBrite.Builder().build();
        // TODO: switch to the rxjava2 scheduler when new sqlbrite will be released
        BriteDatabase briteDatabase = sqlBrite.wrapDatabaseHelper(openHelper, Schedulers.io());

        if (BuildConfig.DEBUG) {
            briteDatabase.setLoggingEnabled(true);
        }

        briteDatabaseHelper = new BriteDatabaseHelper(briteDatabase);

        Dependencies dependencies = new Dependencies(context, this, briteDatabaseHelper,
                schedulerProvider);

        artistRepository = new ArtistRepository(dependencies);
        locationRepository = new LocationRepository(dependencies);
        concertRepository = new ConcertRepository(dependencies);
        syncStateRepository = new SyncStateRepository(dependencies);
        syncRepository = new SyncRepository(dependencies);
    }

    @Override
    public Observable<List<Artist>> getArtists() {
        return artistRepository.getArtists();
    }

    @Override
    public Single<Artist> getArtist(String id) {
        return artistRepository.getArtist(id);
    }

    @Override
    public Completable saveArtist(Artist artist) {
        return artistRepository.saveArtist(artist);
    }

    @Override
    public Completable saveArtists(List<Artist> artists) {
        return artistRepository.saveArtists(artists);
    }

    @Override
    public Completable updateArtist(Artist artist) {
        return artistRepository.updateArtist(artist);
    }

    @Override
    public Completable deleteArtist(Artist artist) {
        return artistRepository.deleteArtist(artist);
    }

    @Override
    public Completable deleteArtists(List<Artist> artists) {
        return artistRepository.deleteArtists(artists);
    }

    @Override
    public Single<List<String>> loadArtistsFromGoogleMusic() {
        return artistRepository.loadArtistsFromGoogleMusic();
    }

    @Override
    public Observable<List<Location>> getLocations() {
        return locationRepository.getLocations();
    }

    @Override
    public Single<Location> getLocation(String id) {
        return locationRepository.getLocation(id);
    }

    @Override
    public Completable saveLocation(Location location) {
        return locationRepository.saveLocation(location);
    }

    @Override
    public Completable saveLocations(List<Location> locations) {
        return locationRepository.saveLocations(locations);
    }

    @Override
    public Completable deleteLocation(Location location) {
        return locationRepository.deleteLocation(location);
    }

    @Override
    public Single<List<Location>> getAvailableLocations() {
        return locationRepository.getAvailableLocations();
    }

    @Override
    public Observable<List<Concert>> getConcerts() {
        return concertRepository.getConcerts();
    }

    @Override
    public Single<Concert> getConcert(String id) {
        return concertRepository.getConcert(id);
    }

    @Override
    public Completable saveConcert(Concert concert) {
        return concertRepository.saveConcert(concert);
    }

    @Override
    public Completable saveConcerts(List<Concert> concerts) {
        return concertRepository.saveConcerts(concerts);
    }

    @Override
    public Completable deleteConcert(Concert concert) {
        return concertRepository.deleteConcert(concert);
    }

    @Override
    public Single<List<SyncState>> getSyncStates() {
        return syncStateRepository.getSyncStates();
    }

    @Override
    public Single<List<SyncState>> getSyncStatesToUpdate(Date currentTime,
                                                         long relevancePeriodHours) {
        return syncStateRepository.getSyncStatesToUpdate(currentTime, relevancePeriodHours);
    }

    @Override
    public Single<Boolean> isSyncRequired(Date currentTime, long relevancePeriodHours) {
        return syncStateRepository.isSyncRequired(currentTime, relevancePeriodHours);
    }

    @Override
    public Completable saveSyncState(SyncState syncState) {
        return syncStateRepository.saveSyncState(syncState);
    }

    @Override
    public Single<SyncResult> syncData(Date currentTime, long relevancePeriodHours) {
        return syncRepository.syncData(currentTime, relevancePeriodHours);
    }

    @Override
    public void onSyncInterrupted() {
        syncRepository.onSyncInterrupted();
    }

    @Override
    public void close() {
        briteDatabaseHelper.close();
    }
}
