package io.github.andreybgm.gigreminder.repository.sync;

import android.accounts.Account;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import java.util.Date;
import java.util.List;

import io.github.andreybgm.gigreminder.BuildConfig;
import io.github.andreybgm.gigreminder.R;
import io.github.andreybgm.gigreminder.data.Artist;
import io.github.andreybgm.gigreminder.data.Concert;
import io.github.andreybgm.gigreminder.repository.DataSource;
import io.github.andreybgm.gigreminder.repository.RepositoryProvider;
import io.github.andreybgm.gigreminder.screen.concertdetails.ConcertDetailsActivity;
import io.github.andreybgm.gigreminder.screen.main.MainActivity;
import io.reactivex.Observable;

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
                .doOnSuccess(this::sendNotification)
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

    private void sendNotification(AppSyncResult syncResult) {
        Context context = getContext();
        List<Concert> newConcerts = syncResult.getNewConcerts();

        if (newConcerts.size() == 0) {
            return;
        }

        final int pendingIntentRequestCode = 0;
        final int pendingIntentFlags = PendingIntent.FLAG_CANCEL_CURRENT;
        final PendingIntent pendingIntent;
        final String contentTitle;
        final String contentText;

        if (newConcerts.size() == 1) {
            Concert concert = newConcerts.get(0);
            Intent intent = ConcertDetailsActivity.makeIntent(context, concert);

            pendingIntent = TaskStackBuilder.create(context)
                    .addParentStack(ConcertDetailsActivity.class)
                    .addNextIntent(intent)
                    .getPendingIntent(pendingIntentRequestCode, pendingIntentFlags);

            contentTitle = context.getString(R.string.notification_new_concert);
            contentText = concert.getArtist().getName();
        } else {
            Intent intent = MainActivity.makeIntent(context);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            pendingIntent = PendingIntent.getActivity(
                    context, pendingIntentRequestCode, intent, pendingIntentFlags);

            contentTitle = context.getString(R.string.notification_new_concerts);
            contentText = TextUtils.join(", ",
                    Observable.fromIterable(newConcerts)
                            .map(Concert::getArtist)
                            .distinct()
                            .map(Artist::getName)
                            .sorted(String::compareTo)
                            .take(10)
                            .toList()
                            .blockingGet());
        }

        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.notification_new_concert)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        NotificationManager systemService =
                (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
        systemService.notify(0, notification);
    }

    @Override
    public void onSyncCanceled() {
        super.onSyncCanceled();
        repository.onSyncInterrupted();
    }
}
