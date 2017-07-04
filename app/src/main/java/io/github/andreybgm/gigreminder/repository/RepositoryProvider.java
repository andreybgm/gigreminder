package io.github.andreybgm.gigreminder.repository;

import android.content.Context;

public class RepositoryProvider {
    private static DataSource repository;

    public static synchronized DataSource provideRepository(Context context) {
        if (repository == null) {
            repository = new Repository(context);
        }

        return repository;
    }

    public static synchronized void reset() {
        if (repository != null) {
            repository.close();
            repository = null;
        }
    }
}
