package io.github.andreybgm.gigreminder.repository.entityrepository;

import android.database.sqlite.SQLiteConstraintException;
import android.support.annotation.NonNull;

import com.squareup.sqlbrite.BriteDatabase;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import io.github.andreybgm.gigreminder.Injection;
import io.github.andreybgm.gigreminder.data.Artist;
import io.github.andreybgm.gigreminder.data.Location;
import io.github.andreybgm.gigreminder.repository.db.Contract;
import io.github.andreybgm.gigreminder.repository.db.entity.ArtistEntity;
import io.github.andreybgm.gigreminder.repository.sync.SyncManager;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public class ArtistRepository extends BaseEntityRepository {

    private final SyncStateRepository syncStateRepository;

    public ArtistRepository(@NonNull Dependencies dependencies) {
        super(dependencies);
        syncStateRepository = new SyncStateRepository(dependencies);
    }

    public Observable<List<Artist>> getArtists() {
        return dbHelper.selectAllAsNotifiedObservable(entityRegistry.artist,
                Contract.ArtistsTable.COLUMN_NAME + " ASC, " + Contract.ArtistsTable.COLUMN_ID);
    }

    public Single<Artist> getArtist(String id) {
        return dbHelper.selectObjectFromTable(entityRegistry.artist, id)
                .subscribeOn(schedulerProvider.io());
    }

    public Completable saveArtist(Artist artist) {
        return saveArtists(Collections.singletonList(artist));
    }

    public Completable saveArtists(List<Artist> artists) {
        return Completable.fromAction(() -> {
            try (BriteDatabase.Transaction transaction =
                         dbHelper.getBriteDatabase().newTransaction()) {
                List<Location> locations = dbHelper.selectAll(entityRegistry.location,
                        entityRegistry.location.getIdColumn());
                Observable<Location> locationObservable = Observable.fromIterable(locations);

                Observable.fromIterable(artists).blockingForEach(artist -> {
                            dbHelper.getBriteDatabase().insert(
                                    entityRegistry.artist.getTableName(),
                                    entityRegistry.artist.toContentValues(artist));
                            syncStateRepository.resetSyncStates(locationObservable, artist);
                        }
                );

                transaction.markSuccessful();
            } catch (Throwable e) {
                if (e instanceof SQLiteConstraintException) {
                    ArtistEntity.handleSqlConstraintException((SQLiteConstraintException) e);
                }
                throw e;
            }
        })
                .doOnComplete(() -> SyncManager.requestSync(context))
                .subscribeOn(schedulerProvider.io());
    }

    public Completable updateArtist(Artist artist) {
        return Completable.fromAction(() -> {
            try (BriteDatabase.Transaction transaction =
                         dbHelper.getBriteDatabase().newTransaction()) {
                int rowCount = dbHelper.getBriteDatabase().update(
                        entityRegistry.artist.getTableName(),
                        entityRegistry.artist.toContentValues(artist),
                        entityRegistry.artist.getIdColumn() + "=?",
                        artist.getId());

                if (rowCount == 0) {
                    throw dbHelper.createDataNotFoundError(
                            entityRegistry.artist.getTableName(), artist.getId());
                }

                List<Location> locations = dbHelper.selectAll(entityRegistry.location,
                        entityRegistry.location.getIdColumn());
                syncStateRepository.resetSyncStates(Observable.fromIterable(locations),
                        artist);

                transaction.markSuccessful();
            } catch (SQLiteConstraintException e) {
                ArtistEntity.handleSqlConstraintException(e);
            }
        })
                .doOnComplete(() -> SyncManager.requestSync(context))
                .subscribeOn(schedulerProvider.io());
    }

    public Completable deleteArtist(Artist artist) {
        return deleteArtists(Collections.singletonList(artist));
    }

    public Completable deleteArtists(List<Artist> artists) {
        return Completable.fromAction(() -> {
            try (BriteDatabase.Transaction transaction =
                         dbHelper.getBriteDatabase().newTransaction()) {
                Observable.fromIterable(artists).blockingForEach(artist -> {
                    dbHelper.blockingDeleteByValue(entityRegistry.artist.getTableName(),
                            entityRegistry.artist.getIdColumn(),
                            artist.getId());
                    dbHelper.blockingDeleteByValue(entityRegistry.concert.getTableName(),
                            Contract.ConcertsTable.COLUMN_ARTIST_ID, artist.getId());
                    dbHelper.blockingDeleteByValue(entityRegistry.syncState.getTableName(),
                            Contract.SyncStatesTable.COLUMN_ARTIST_ID, artist.getId());
                });
                transaction.markSuccessful();
            }
        })
                .subscribeOn(schedulerProvider.io());
    }

    public Single<List<String>> loadArtistsFromGoogleMusic() {
        return Single.fromCallable(() ->
                Injection.provideInjection()
                        .provideGoogleMusicSource()
                        .loadArtists(context))
                .subscribeOn(schedulerProvider.io())
                .zipWith(
                        getArtists()
                                .take(1)
                                .flatMap(Observable::fromIterable)
                                .map(Artist::getName)
                                .map(this::normalizeName)
                                .toList()
                                .map(HashSet::new),
                        (newNames, existedNames) -> Observable.fromIterable(newNames)
                                .filter(newName -> !existedNames.contains(normalizeName(newName)))
                                .toList()
                )
                .flatMap(single -> single);
    }

    boolean doesArtistExist(Artist artist) {
        return dbHelper.blockingDoesObjectExist(entityRegistry.artist, artist.getId());
    }

    private String normalizeName(String name) {
        return name.toLowerCase().trim();
    }
}
