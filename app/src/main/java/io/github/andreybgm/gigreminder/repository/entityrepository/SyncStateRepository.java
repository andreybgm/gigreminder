package io.github.andreybgm.gigreminder.repository.entityrepository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.github.andreybgm.gigreminder.data.Artist;
import io.github.andreybgm.gigreminder.data.Location;
import io.github.andreybgm.gigreminder.data.SyncState;
import io.github.andreybgm.gigreminder.repository.db.Contract;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public class SyncStateRepository extends BaseEntityRepository {

    public SyncStateRepository(@NonNull Dependencies dependencies) {
        super(dependencies);
    }

    public Single<List<SyncState>> getSyncStates() {
        return dbHelper.selectAllAsNotifiedObservable(entityRegistry.syncState,
                entityRegistry.syncState.getIdColumn())
                .firstOrError();
    }

    public Single<List<SyncState>> getSyncStatesToUpdate(Date currentTime,
                                                         long relevancePeriodHours) {
        return Single.fromCallable(() -> {
            String columns = dbHelper.makeColumnsString(entityRegistry.syncState);
            String time = String.valueOf(
                    currentTime.getTime() - TimeUnit.HOURS.toMillis(relevancePeriodHours));
            String where = String.format(
                    "%s<=?", Contract.SyncStatesTable.COLUMN_LAST_SYNC_TIME);
            String sql = String.format("SELECT %s FROM %s WHERE %s ORDER BY %s",
                    columns,
                    entityRegistry.syncState.getTableName(),
                    where,
                    entityRegistry.syncState.getIdColumn());

            List<SyncState> syncStates = new ArrayList<>();

            try (Cursor cursor = dbHelper.getBriteDatabase().query(sql, time)) {
                while (cursor.moveToNext()) {
                    syncStates.add(entityRegistry.syncState.fromCursor(cursor));
                }
            }

            return syncStates;
        })
                .subscribeOn(schedulerProvider.io());
    }

    public Single<Boolean> isSyncRequired(Date currentTime, long relevancePeriodHours) {
        return getSyncStatesToUpdate(currentTime, relevancePeriodHours)
                .map(syncStates -> !syncStates.isEmpty());
    }

    public Completable saveSyncState(SyncState syncState) {
        return dbHelper.saveObjects(entityRegistry.syncState, Collections.singletonList(syncState),
                null, SQLiteDatabase.CONFLICT_REPLACE)
                .subscribeOn(schedulerProvider.io());
    }

    void resetSyncStates(Observable<Location> locationObservable, Artist artist) {
        locationObservable
                .subscribe(location -> resetSyncState(artist, location));
    }

    void resetSyncStates(Observable<Artist> artistObservable, Location location) {
        artistObservable
                .subscribe(artist -> resetSyncState(artist, location));
    }

    private void resetSyncState(Artist artist, Location location) {
        SyncState syncState = new SyncState(artist, location);
        dbHelper.getBriteDatabase().insert(
                entityRegistry.syncState.getTableName(),
                entityRegistry.syncState.toContentValues(syncState),
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    void blockingSave(SyncState syncState) {
        dbHelper.blockingSaveObjects(entityRegistry.syncState, Collections.singletonList(syncState),
                SQLiteDatabase.CONFLICT_REPLACE);
    }
}
