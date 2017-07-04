package io.github.andreybgm.gigreminder;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;

public class GigApplication extends Application {
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    private static String SYNC_ACCOUNT_TYPE;
    private static String SYNC_AUTHORITY;

    @Override
    public void onCreate() {
        super.onCreate();

        context = this;

        if (!LeakCanary.isInAnalyzerProcess(this)) {
            LeakCanary.install(this);
        }

        SYNC_ACCOUNT_TYPE = getString(R.string.sync_account_type);
        SYNC_AUTHORITY = getString(R.string.sync_authority);
    }

    public static Context getContext() {
        return context;
    }

    public static String getSyncAccountType() {
        return SYNC_ACCOUNT_TYPE;
    }

    public static String getSyncAuthority() {
        return SYNC_AUTHORITY;
    }
}
