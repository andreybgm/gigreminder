package io.github.andreybgm.gigreminder.repository.entityrepository;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.squareup.sqlbrite.BriteDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.andreybgm.gigreminder.data.Concert;
import io.github.andreybgm.gigreminder.repository.db.Contract;
import io.github.andreybgm.gigreminder.repository.db.DbUtils;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public class ConcertRepository extends BaseEntityRepository {
    private final ArtistRepository artistRepository;
    private final LocationRepository locationRepository;

    public ConcertRepository(@NonNull Dependencies dependencies) {
        super(dependencies);
        artistRepository = new ArtistRepository(dependencies);
        locationRepository = new LocationRepository(dependencies);
    }

    public Observable<List<Concert>> getConcerts() {
        return dbHelper.selectAllAsNotifiedObservable(entityRegistry.concert,
                Contract.ConcertsTable.COLUMN_DATE + " ASC, " + Contract.ConcertsTable.COLUMN_ID);
    }

    public Single<Concert> getConcert(String id) {
        return dbHelper.selectObjectFromTable(entityRegistry.concert, id)
                .subscribeOn(schedulerProvider.io());
    }

    public Completable saveConcert(Concert concert) {
        return saveConcerts(Collections.singletonList(concert));
    }

    public Completable saveConcerts(List<Concert> concerts) {
        return Completable.fromAction(() -> blockingSave(concerts))
                .subscribeOn(schedulerProvider.io());
    }

    SavingResult blockingSave(List<Concert> concerts) {
        String concertSelectionSql = "SELECT "
                + Contract.ConcertsTable.COLUMN_ID
                + " FROM "
                + Contract.ConcertsTable.TABLE_NAME
                + " WHERE "
                + Contract.ConcertsTable.COLUMN_API_CODE + "=?";

        List<Concert> newConcerts = new ArrayList<>();

        try (BriteDatabase.Transaction transaction =
                     dbHelper.getBriteDatabase().newTransaction()) {
            Observable.fromIterable(concerts)
                    .filter(concert -> artistRepository.doesArtistExist(concert.getArtist())
                            && locationRepository.doesLocationExist(concert.getLocation())
                    )
                    .blockingForEach(concert -> {
                        try (Cursor cursor = dbHelper.getBriteDatabase().query(
                                concertSelectionSql, concert.getApiCode())) {
                            boolean concertExists = cursor.moveToFirst();

                            if (concertExists) {
                                String id = DbUtils.getStringFromCursor(cursor,
                                        Contract.ConcertsTable.COLUMN_ID);
                                updateConcert(id, concert);
                            } else {
                                dbHelper.getBriteDatabase().insert(
                                        Contract.ConcertsTable.TABLE_NAME,
                                        entityRegistry.concert.toContentValues(concert));
                                newConcerts.add(concert);
                            }
                        }
                    });
            transaction.markSuccessful();
        }

        return new SavingResult(newConcerts);
    }

    public Completable deleteConcert(Concert concert) {
        return Completable.fromAction(() ->
                dbHelper.blockingDeleteByValue(entityRegistry.concert.getTableName(),
                        entityRegistry.concert.getIdColumn(), concert.getId())
        )
                .subscribeOn(schedulerProvider.io());
    }

    private void updateConcert(String id, Concert concert) {
        Concert updatedConcert = new Concert.Builder(
                id, concert.getApiCode(), concert.getArtist(), concert.getLocation())
                .date(concert.getDate())
                .place(concert.getPlace())
                .url(concert.getUrl())
                .imageUrl(concert.getImageUrl())
                .build();
        dbHelper.getBriteDatabase().update(Contract.ConcertsTable.TABLE_NAME,
                entityRegistry.concert.toContentValues(updatedConcert),
                Contract.ConcertsTable.COLUMN_ID + "=?",
                id);
    }

    static class SavingResult {
        @NonNull
        private final List<Concert> newConcerts;

        private SavingResult(@NonNull List<Concert> newConcerts) {
            this.newConcerts = newConcerts;
        }

        @NonNull
        List<Concert> getNewConcerts() {
            return newConcerts;
        }
    }
}
