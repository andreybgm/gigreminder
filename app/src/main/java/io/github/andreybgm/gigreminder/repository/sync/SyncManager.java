package io.github.andreybgm.gigreminder.repository.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import io.github.andreybgm.gigreminder.BuildConfig;
import io.github.andreybgm.gigreminder.GigApplication;
import io.github.andreybgm.gigreminder.R;

public final class SyncManager {

    static final int RELEVANCE_PERIOD_HOURS = 24;

    private static final String LOG_TAG = SyncManager.class.getSimpleName();
    private static final String BUNDLE_ATTEMPT = "ATTEMPT";
    private static final int SYNC_PERIOD_HOURS = 6;
    private static final int DEBUG_SYNC_PERIOD_HOURS = 4;
    private static final int MAX_REPEATED_SYNC_ATTEMPT = 10;

    public static void init(Context context) {
        Account account = makeAccount(context);
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        if (accountManager.addAccountExplicitly(account, null, null)) {
            ContentResolver.setSyncAutomatically(account, GigApplication.getSyncAuthority(), true);
            int syncPeriod = BuildConfig.DEBUG ? DEBUG_SYNC_PERIOD_HOURS : SYNC_PERIOD_HOURS;
            ContentResolver.addPeriodicSync(account, GigApplication.getSyncAuthority(), Bundle.EMPTY,
                    TimeUnit.HOURS.toSeconds(syncPeriod));
        }

        if (ContentResolver.getIsSyncable(account, GigApplication.getSyncAuthority()) <= 0) {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "setIsSyncable");
            }

            ContentResolver.setIsSyncable(account, GigApplication.getSyncAuthority(), 1);
        }
    }

    @NonNull
    private static Account makeAccount(Context context) {
        return new Account(context.getString(R.string.sync_account_name), GigApplication.getSyncAccountType());
    }

    public static void requestSync(Context context) {
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "Sync is requested");
        }

        ContentResolver.requestSync(makeAccount(context), GigApplication.getSyncAuthority(), new Bundle());
    }

    public static void requestSync(Context context, Bundle previousExtras) {
        int attempt = previousExtras.getInt(BUNDLE_ATTEMPT, 0);

        if (attempt > MAX_REPEATED_SYNC_ATTEMPT) {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, String.format("" +
                        "Repeated sync is requested: attempt %s: denied", attempt));
            }

            return;
        }

        Bundle newExtras = new Bundle();
        newExtras.putInt(BUNDLE_ATTEMPT, attempt + 1);

        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, String.format("Repeated sync is requested: attempt %s", attempt));
        }

        ContentResolver.requestSync(makeAccount(context), GigApplication.getSyncAuthority(), newExtras);
    }

    public static void disableSync(Context context) {
        Account account = makeAccount(context);
        ContentResolver.cancelSync(account, GigApplication.getSyncAuthority());
        ContentResolver.setIsSyncable(account, GigApplication.getSyncAuthority(), 0);
    }
}
