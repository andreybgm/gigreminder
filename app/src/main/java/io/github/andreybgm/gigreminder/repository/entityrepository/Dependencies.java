package io.github.andreybgm.gigreminder.repository.entityrepository;

import android.content.Context;
import android.support.annotation.NonNull;

import io.github.andreybgm.gigreminder.repository.DataSource;
import io.github.andreybgm.gigreminder.repository.db.BriteDatabaseHelper;
import io.github.andreybgm.gigreminder.utils.schedulers.SchedulerProvider;

public class Dependencies {
    @NonNull
    final Context context;

    @NonNull
    final DataSource repository;

    @NonNull
    final BriteDatabaseHelper dbHelper;

    @NonNull
    final SchedulerProvider schedulerProvider;

    public Dependencies(@NonNull Context context,
                        @NonNull DataSource repository,
                        @NonNull BriteDatabaseHelper dbHelper,
                        @NonNull SchedulerProvider schedulerProvider) {
        this.context = context;
        this.repository = repository;
        this.dbHelper = dbHelper;
        this.schedulerProvider = schedulerProvider;
    }
}
