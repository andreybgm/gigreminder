package io.github.andreybgm.gigreminder.repository.entityrepository;

import android.content.Context;
import android.support.annotation.NonNull;

import io.github.andreybgm.gigreminder.repository.DataSource;
import io.github.andreybgm.gigreminder.repository.db.BriteDatabaseHelper;
import io.github.andreybgm.gigreminder.repository.db.entity.EntityRegistry;
import io.github.andreybgm.gigreminder.utils.schedulers.SchedulerProvider;

class BaseEntityRepository {
    @NonNull
    final Context context;

    @NonNull
    final DataSource repository;

    @NonNull
    final BriteDatabaseHelper dbHelper;

    @NonNull
    final SchedulerProvider schedulerProvider;

    final EntityRegistry entityRegistry;

    BaseEntityRepository(@NonNull Dependencies dependencies) {
        this.context = dependencies.context;
        this.repository = dependencies.repository;
        this.dbHelper = dependencies.dbHelper;
        this.schedulerProvider = dependencies.schedulerProvider;
        this.entityRegistry = new EntityRegistry(repository);
    }
}
