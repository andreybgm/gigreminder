package io.github.andreybgm.gigreminder.repository.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import java.util.Date;

import io.github.andreybgm.gigreminder.BuildConfig;
import io.github.andreybgm.gigreminder.repository.DataSource;
import io.github.andreybgm.gigreminder.repository.RepositoryProvider;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private final static String LOG_TAG = SyncManager.class.getSimpleName();
    private final DataSource repository;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        repository = RepositoryProvider.provideRepository(
                getContext().getApplicationContext());
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        repository = RepositoryProvider.provideRepository(
                getContext().getApplicationContext());
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        repository.syncData(new Date(), SyncManager.RELEVANCE_PERIOD_HOURS)
                .flatMap(noValue -> repository.isSyncRequired(
                        new Date(), SyncManager.RELEVANCE_PERIOD_HOURS))
                .toObservable()
                .takeLast(1)
                .blockingSubscribe(
                        syncIsRequired -> {
                            if (syncIsRequired) {
                                SyncManager.requestSync(getContext(), extras);
                            }
                        },
                        e -> {
                            if (BuildConfig.DEBUG) {
                                Log.e(LOG_TAG, "The sync error", e);
                            }

                            if (e instanceof RuntimeException) {
                                syncResult.stats.numParseExceptions++;
                            } else {
                                syncResult.stats.numIoExceptions++;
                            }
                        }
                );
    }

    @Override
    public void onSyncCanceled() {
        super.onSyncCanceled();
        repository.onSyncInterrupted();
    }
}
