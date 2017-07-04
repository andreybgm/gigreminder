package io.github.andreybgm.gigreminder.repository.entityrepository;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.squareup.sqlbrite.BriteDatabase;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.andreybgm.gigreminder.api.ApiFactory;
import io.github.andreybgm.gigreminder.data.Artist;
import io.github.andreybgm.gigreminder.data.Location;
import io.github.andreybgm.gigreminder.repository.db.Contract;
import io.github.andreybgm.gigreminder.repository.db.DbUtils;
import io.github.andreybgm.gigreminder.repository.sync.SyncManager;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public class LocationRepository extends BaseEntityRepository {
    private final SyncStateRepository syncStateRepository;
    private volatile CachedLocations cachedApiLocations;

    public LocationRepository(@NonNull Dependencies dependencies) {
        super(dependencies);
        syncStateRepository = new SyncStateRepository(dependencies);
        cachedApiLocations = new CachedLocations();
    }

    public Observable<List<Location>> getLocations() {
        return dbHelper.selectAllAsNotifiedObservable(entityRegistry.location,
                Contract.LocationsTable.COLUMN_NAME + " ASC, " + Contract.LocationsTable.COLUMN_ID);
    }

    public Single<Location> getLocation(String id) {
        return dbHelper.selectObjectFromTable(entityRegistry.location, id)
                .subscribeOn(schedulerProvider.io());
    }

    public Completable saveLocation(Location location) {
        return saveLocations(Collections.singletonList(location));
    }

    public Completable saveLocations(List<Location> locations) {
        return Completable.fromAction(() -> {
            try (BriteDatabase.Transaction transaction =
                         dbHelper.getBriteDatabase().newTransaction()) {
                List<Artist> artists = dbHelper.selectAll(entityRegistry.artist,
                        entityRegistry.artist.getIdColumn());
                Observable<Artist> artistObservable = Observable.fromIterable(artists);

                Observable.fromIterable(locations).blockingForEach(location -> {
                    dbHelper.getBriteDatabase().insert(
                            entityRegistry.location.getTableName(),
                            entityRegistry.location.toContentValues(location));
                    syncStateRepository.resetSyncStates(artistObservable, location);
                });

                transaction.markSuccessful();
            }
        })
                .doOnComplete(() -> SyncManager.requestSync(context))
                .subscribeOn(schedulerProvider.io());
    }

    public Completable deleteLocation(Location location) {
        return Completable.fromAction(() -> {
            try (BriteDatabase.Transaction transaction =
                         dbHelper.getBriteDatabase().newTransaction()) {
                dbHelper.blockingDeleteByValue(entityRegistry.location.getTableName(),
                        entityRegistry.location.getIdColumn(), location.getId());
                dbHelper.blockingDeleteByValue(entityRegistry.concert.getTableName(),
                        Contract.ConcertsTable.COLUMN_LOCATION_ID, location.getId());
                dbHelper.blockingDeleteByValue(entityRegistry.syncState.getTableName(),
                        Contract.SyncStatesTable.COLUMN_LOCATION_ID, location.getId());

                transaction.markSuccessful();
            }
        })
                .subscribeOn(schedulerProvider.io());
    }

    public Single<List<Location>> getAvailableLocations() {
        Observable<List<Location>> cachedLocationsObservable =
                Observable.just(cachedApiLocations)
                        .filter(CachedLocations::hasCache)
                        .map(CachedLocations::getCachedLocations);

        Observable<List<Location>> apiLocationsObservable =
                ApiFactory.getConcertService().locations()
                        .flatMap(responses -> Observable.fromIterable(responses)
                                .map(response -> new Location(
                                        response.getApiCode(),
                                        response.getName()
                                ))
                                .sorted((location1, location2) ->
                                        location1.getName().compareTo(location2.getName())))
                        .toList()
                        .doOnSuccess(locations ->
                                cachedApiLocations = new CachedLocations(locations))
                        .toObservable();

        return Observable.concat(cachedLocationsObservable, apiLocationsObservable)
                .firstOrError()
                .zipWith(getLocalLocationApiCodes(),
                        (apiLocations, localApiCodes) ->
                                Observable.fromIterable(apiLocations)
                                        .filter(location ->
                                                !localApiCodes.contains(location.getApiCode()))
                                        .toList())
                .flatMap(single -> single)
                .subscribeOn(schedulerProvider.io());
    }

    boolean doesLocationExist(Location location) {
        return dbHelper.blockingDoesObjectExist(entityRegistry.location, location.getId());
    }

    private Single<Set<String>> getLocalLocationApiCodes() {
        return Single.fromCallable(() -> {
            final String sql = "SELECT "
                    + Contract.LocationsTable.COLUMN_API_CODE
                    + " FROM "
                    + Contract.LocationsTable.TABLE_NAME;
            final Set<String> codes = new HashSet<>();

            try (Cursor cursor = dbHelper.getBriteDatabase().query(sql)) {
                while (cursor.moveToNext()) {
                    String code = DbUtils.getStringFromCursor(
                            cursor, Contract.LocationsTable.COLUMN_API_CODE);
                    codes.add(code);
                }
            }

            return codes;
        });
    }

    private static class CachedLocations {
        @NonNull
        final List<Location> cachedLocations;
        final boolean hasCache;

        CachedLocations() {
            this.hasCache = false;
            this.cachedLocations = Collections.emptyList();
        }

        CachedLocations(@NonNull List<Location> cachedLocations) {
            this.hasCache = true;
            this.cachedLocations = cachedLocations;
        }

        boolean hasCache() {
            return hasCache;
        }

        @NonNull
        List<Location> getCachedLocations() {
            return cachedLocations;
        }
    }
}
