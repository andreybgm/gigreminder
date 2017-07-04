package io.github.andreybgm.gigreminder.repository.entityrepository;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.squareup.sqlbrite.BriteDatabase;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import io.github.andreybgm.gigreminder.BuildConfig;
import io.github.andreybgm.gigreminder.R;
import io.github.andreybgm.gigreminder.api.ApiFactory;
import io.github.andreybgm.gigreminder.api.ConcertService;
import io.github.andreybgm.gigreminder.api.response.EventResponse;
import io.github.andreybgm.gigreminder.api.response.PlaceResponse;
import io.github.andreybgm.gigreminder.api.response.SearchResponse;
import io.github.andreybgm.gigreminder.data.Artist;
import io.github.andreybgm.gigreminder.data.Concert;
import io.github.andreybgm.gigreminder.data.Location;
import io.github.andreybgm.gigreminder.data.SyncState;
import io.github.andreybgm.gigreminder.repository.sync.eventbus.SyncEventBus;
import io.github.andreybgm.gigreminder.repository.sync.eventbus.SyncFinishEvent;
import io.github.andreybgm.gigreminder.repository.sync.eventbus.SyncStartEvent;
import io.github.andreybgm.gigreminder.screen.concertdetails.ConcertDetailsActivity;
import io.github.andreybgm.gigreminder.screen.main.MainActivity;
import io.github.andreybgm.gigreminder.utils.Pair;
import io.reactivex.Observable;
import io.reactivex.Single;

public class SyncRepository extends BaseEntityRepository {

    private static final String LOG_TAG = SyncRepository.class.getSimpleName();
    private static final String EVENT_CATEGORY_CONCERT = "concert";

    private final ConcertRepository concertRepository;
    private final SyncStateRepository syncStateRepository;

    public SyncRepository(@NonNull Dependencies dependencies) {
        super(dependencies);

        concertRepository = new ConcertRepository(dependencies);
        syncStateRepository = new SyncStateRepository(dependencies);
    }

    public Single<SyncResult> syncData(Date currentTime, long relevancePeriodHours) {
        ConcertService apiService = ApiFactory.getConcertService();

        return syncStateRepository.getSyncStatesToUpdate(currentTime, relevancePeriodHours)
                .doOnSubscribe(disposable -> {
                    if (BuildConfig.DEBUG) {
                        Log.d(LOG_TAG, "Sync is started");
                    }

                    SyncEventBus.sendEvent(SyncStartEvent.create());
                })
                .toObservable()
                .flatMap(Observable::fromIterable)
                .flatMap(syncState -> sync(syncState, apiService).toObservable())
                .reduceWith(SyncResult::new, SyncResult::mergeWith)
                .doOnSuccess(this::sendNotification)
                .doOnEvent((r, e) -> {
                    if (BuildConfig.DEBUG) {
                        Log.d(LOG_TAG, "Sync is finished");
                    }

                    sendSyncFinishEvent();
                });
    }

    public void onSyncInterrupted() {
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "Sync is interrupted");
        }

        sendSyncFinishEvent();
    }

    private void sendNotification(SyncResult syncResult) {
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

    private void sendSyncFinishEvent() {
        SyncEventBus.sendEvent(SyncFinishEvent.create());
    }

    private Single<SyncResult> sync(SyncState syncState, ConcertService apiService) {
        Artist artist = syncState.getArtist();
        Location location = syncState.getLocation();

        return loadConcerts(artist, location, apiService)
                .toList()
                .map(concerts -> {
                    ConcertRepository.SavingResult savingResult;

                    try (BriteDatabase.Transaction transaction =
                                 dbHelper.getBriteDatabase().newTransaction()) {
                        SyncState newSyncState = new SyncState(
                                syncState.getArtist(), syncState.getLocation(), new Date());

                        syncStateRepository.blockingSave(newSyncState);
                        savingResult = concertRepository.blockingSave(concerts);

                        transaction.markSuccessful();
                    }

                    return new SyncResult(savingResult.getNewConcerts());
                });
    }

    private Observable<Concert> loadConcerts(Artist artist, Location location,
                                             ConcertService apiService) {
        String name = artist.getName().toLowerCase();

        return apiService.search(name, location.getApiCode())
                .flatMap(searchResponse -> Observable.fromIterable(searchResponse.getResults()))
//                .filter(searchResult -> isResultTitleContainName(searchResult, name))
                .flatMap(searchResult -> loadConcertData(searchResult, name, apiService))
                .map(concertData -> {
                    SearchResponse.Result searchResult = concertData.searchResult;
                    EventResponse eventResponse = concertData.eventResponse;
                    PlaceResponse placeResponse = concertData.placeResponse;

                    String apiCode = String.valueOf(eventResponse.getId());
                    Date date = new Date(TimeUnit.SECONDS.toMillis(
                            eventResponse.getDates().get(0).getStart()));
                    String imageUrl = getImageUrl(searchResult);
                    String placeTitle = getPlaceTitle(placeResponse);

                    return new Concert.Builder(apiCode, artist, location)
                            .date(date)
                            .place(placeTitle)
                            .url(eventResponse.getUrl())
                            .imageUrl(imageUrl)
                            .build();
                });
    }

    private Observable<ConcertData> loadConcertData(SearchResponse.Result searchResult,
                                                    String name,
                                                    ConcertService apiService) {
        return apiService.eventDetails(searchResult.getId())
                .filter(eventResponse -> isEventConcert(eventResponse)
                        && isEventResponseHaveDate(eventResponse)
                        && isEventTitleContainName(eventResponse, name)
                )
                .flatMap(eventResponse ->
                        apiService.placeDetails(eventResponse.getPlace().getId())
                                .map(placeResponse -> Pair.create(eventResponse, placeResponse))
                )
                .map(eventAndPlace ->
                        new ConcertData(searchResult, eventAndPlace.first, eventAndPlace.second));
    }

    private String getPlaceTitle(PlaceResponse placeResponse) {
        if (!placeResponse.getShortTitle().isEmpty()) {
            return placeResponse.getShortTitle();
        }

        return placeResponse.getTitle();
    }

    private String getImageUrl(SearchResponse.Result searchResult) {
        SearchResponse.Image firstImage = searchResult.getFirstImage();

        if (firstImage == null) {
            return "";
        }

        String imgUrl = firstImage.getThumbnails().getImg640x384();

        return imgUrl == null ? "" : imgUrl;
    }

    private boolean isEventConcert(EventResponse eventResponse) {
        List<String> categories = eventResponse.getCategories();

        return categories != null && categories.contains(EVENT_CATEGORY_CONCERT);

    }

    private boolean isEventTitleContainName(EventResponse eventResponse, String name) {
        String shortTitle = eventResponse.getShortTitle();
        boolean shortNameMatches = shortTitle != null && shortTitle.equalsIgnoreCase(name);

        return shortNameMatches || isFullTitleContainName(eventResponse, name);
    }

    private boolean isFullTitleContainName(EventResponse eventResponse, String name) {
        Pattern pattern = Pattern.compile(
                "(?ui)(concert|концерт|концерт группы)?[\\p{Punct}\\p{Space}]*"
                        + name
                        + "[\\p{Punct}\\p{Space}]*");

        return pattern.matcher(eventResponse.getTitle().toLowerCase()).matches();
    }

    private boolean isEventResponseHaveDate(EventResponse eventResponse) {
        List<EventResponse.EventDate> dates = eventResponse.getDates();

        return dates != null && dates.size() != 0;
    }

    private static class ConcertData {
        final SearchResponse.Result searchResult;
        final EventResponse eventResponse;
        final PlaceResponse placeResponse;

        ConcertData(SearchResponse.Result searchResult, EventResponse eventResponse,
                    PlaceResponse placeResponse) {
            this.searchResult = searchResult;
            this.eventResponse = eventResponse;
            this.placeResponse = placeResponse;
        }
    }
}
