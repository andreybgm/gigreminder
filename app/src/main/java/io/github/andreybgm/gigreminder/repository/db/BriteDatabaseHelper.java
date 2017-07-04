package io.github.andreybgm.gigreminder.repository.db;

import android.database.Cursor;
import android.text.TextUtils;

import com.squareup.sqlbrite.BriteDatabase;

import java.util.ArrayList;
import java.util.List;

import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.github.andreybgm.gigreminder.repository.db.entity.BaseEntity;
import io.github.andreybgm.gigreminder.repository.error.DataCorruptedException;
import io.github.andreybgm.gigreminder.repository.error.DataNotFoundException;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;

public class BriteDatabaseHelper {
    private final BriteDatabase briteDatabase;

    public BriteDatabaseHelper(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

    public BriteDatabase getBriteDatabase() {
        return briteDatabase;
    }

    public <T> Observable<List<T>> selectAllAsNotifiedObservable(BaseEntity<T> entity,
                                                                 String order) {
        String sql = createSelectAllQuery(entity, order);

        return RxJavaInterop.toV2Observable(
                briteDatabase.createQuery(entity.getTableName(), sql)
                        .mapToList(entity::fromCursor));
    }

    public <T> List<T> selectAll(BaseEntity<T> entity, String order) {
        String sql = createSelectAllQuery(entity, order);
        List<T> objects = new ArrayList<>();

        try (Cursor cursor = briteDatabase.query(sql)) {
            while (cursor.moveToNext()) {
                objects.add(entity.fromCursor(cursor));
            }
        }

        return objects;
    }

    public <T> String makeColumnsString(BaseEntity<T> entity) {
        return TextUtils.join(",", entity.getProjection());
    }

    public <T> Single<T> selectObjectFromTable(BaseEntity<T> entity, String id) {
        return Single.fromCallable(
                () -> {
                    String sql = "SELECT "
                            + makeColumnsString(entity)
                            + " FROM "
                            + entity.getTableName()
                            + " WHERE "
                            + entity.getIdColumn() + "=?";
                    try (Cursor cursor = briteDatabase.query(sql, id)) {
                        int rowCount = cursor.getCount();

                        if (rowCount == 1) {
                            cursor.moveToFirst();

                            return entity.fromCursor(cursor);
                        } else if (rowCount == 0) {
                            throw createDataNotFoundError(entity.getTableName(), id);
                        } else {
                            throw new DataCorruptedException(String.format(
                                    "Found %s entries of %s with the same id %s", rowCount,
                                    entity.getTableName(), id));
                        }
                    }
                });
    }

    public <T> boolean blockingDoesObjectExist(BaseEntity<T> entity, String id) {
        String sql = "SELECT "
                + entity.getIdColumn()
                + " FROM "
                + entity.getTableName()
                + " WHERE "
                + entity.getIdColumn() + "=? "
                + " LIMIT 1";
        try (Cursor cursor = briteDatabase.query(sql, id)) {
            if (cursor.moveToFirst()) {
                return true;
            }
        }

        return false;
    }

    public <T> Completable saveObjects(BaseEntity<T> entity, List<T> objects,
                                       Consumer<Throwable> exceptionHandler,
                                       int conflictAlgorithm) {
        return Completable.fromAction(
                () -> {
                    try {
                        blockingSaveObjects(entity, objects, conflictAlgorithm);
                    } catch (Throwable e) {
                        if (exceptionHandler != null) {
                            exceptionHandler.accept(e);
                        }

                        throw e;
                    }
                });
    }

    public <T> void blockingSaveObjects(BaseEntity<T> entity, List<T> objects,
                                        int conflictAlgorithm) {
        try (BriteDatabase.Transaction transaction = briteDatabase.newTransaction()) {
            Observable.fromIterable(objects).blockingForEach(object ->
                    briteDatabase.insert(
                            entity.getTableName(),
                            entity.toContentValues(object),
                            conflictAlgorithm)
            );
            transaction.markSuccessful();
        }
    }

    public void blockingDeleteByValue(String table, String column, String value) {
        briteDatabase.delete(table, column + "=?", value);
    }

    public DataNotFoundException createDataNotFoundError(String table, String id) {
        return new DataNotFoundException(String.format(
                "An entry of %s with id %s was not found", table, id));
    }

    public void close() {
        briteDatabase.close();
    }

    private <T> String createSelectAllQuery(BaseEntity<T> entity, String order) {
        String columns = makeColumnsString(entity);

        return String.format("SELECT %s FROM %s ORDER BY %s",
                columns, entity.getTableName(), order);
    }
}
