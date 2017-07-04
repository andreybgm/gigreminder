package io.github.andreybgm.gigreminder.repository.db.entity;

import android.content.ContentValues;
import android.database.Cursor;

import io.github.andreybgm.gigreminder.repository.DataSource;

public abstract class BaseEntity<T> {

    private final DataSource dataSource;

    public BaseEntity(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public abstract String getTableName();

    public abstract String[] getProjection();

    public abstract String getIdColumn();

    public abstract ContentValues toContentValues(T entity);

    public abstract T fromCursor(Cursor cursor);

    public DataSource getDataSource() {
        return dataSource;
    }
}
