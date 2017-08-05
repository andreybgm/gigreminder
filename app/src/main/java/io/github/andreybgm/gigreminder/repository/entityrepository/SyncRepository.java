package io.github.andreybgm.gigreminder.repository.entityrepository;

import android.support.annotation.NonNull;
import android.util.Log;

import com.squareup.sqlbrite.BriteDatabase;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import io.github.andreybgm.gigreminder.BuildConfig;
import io.github.andreybgm.gigreminder.api.ApiFactory;
import io.github.andreybgm.gigreminder.api.ConcertService;
import io.github.andreybgm.gigreminder.api.response.EventResponse;
import io.github.andreybgm.gigreminder.api.response.PlaceResponse;
import io.github.andreybgm.gigreminder.api.response.SearchResponse;
import io.github.andreybgm.gigreminder.data.Artist;
import io.github.andreybgm.gigreminder.data.Concert;
import io.github.andreybgm.gigreminder.data.Location;
import io.github.andreybgm.gigreminder.data.SyncState;
import io.github.andreybgm.gigreminder.repository.db.Contract.ConcertsTable;
import io.github.andreybgm.gigreminder.repository.sync.AppSyncResult;
import io.github.andreybgm.gigreminder.repository.sync.eventbus.SyncEventBus;
import io.github.andreybgm.gigreminder.repository.sync.eventbus.SyncFinishEvent;
import io.github.andreybgm.gigreminder.repository.sync.eventbus.SyncStartEvent;
import io.github.andreybgm.gigreminder.utils.Pair;
import io.reactivex.Observable;
import io.reactivex.Single;

public class SyncRepository extends BaseEntityRepository {

    public static final int PAST_CONCERT_KEEPING_PERIOD_IN_DAYS = 7;

    private static final String LOG_TAG = SyncRepository.class.getSimpleName();
    private static final String EVENT_CATEGORY_CONCERT = "concert";

    private final ConcertRepository concertRepository;
    private final SyncStateRepository syncStateRepository;

    public SyncRepository(@NonNull Dependencies dependencies) {
        super(dependencies);

        concertRepository = new ConcertRepository(dependencies);
        syncStateRepository = new SyncStateRepository(dependencies);
    }

    public Single<AppSyncResult> syncData(Date currentTime, long relevancePeriodHours) {
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
                .flatMap(syncState -> sync(syncState, apiService, currentTime).toObservable())
                .reduceWith(AppSyncResult::new, AppSyncResult::mergeWith)
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

    private void sendSyncFinishEvent() {
        SyncEventBus.sendEvent(SyncFinishEvent.create());
    }

    private Single<AppSyncResult> sync(SyncState syncState, ConcertService apiService,
                                       Date currentTime) {
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

                    List<Concert> deletedConcerts = deleteNonexistentConcerts(artist, location,
                            concerts, currentTime);
                    List<Concert> newConcerts = Observable.fromIterable(
                            savingResult.getNewConcerts())
                            .filter(concert -> !deletedConcerts.contains(concert))
                            .toList().blockingGet();

                    return new AppSyncResult(newConcerts);
                });
    }
    private List<Concert> deleteNonexistentConcerts(Artist artist, Location location,
                                                    List<Concert> apiConcerts, Date currentTime) {
        Set<String> loadedApiCodes = Observable.fromIterable(apiConcerts)
                .map(Concert::getApiCode)
                .reduce(new HashSet<String>(), (set, apiCode) -> {
                    set.add(apiCode);
                    return set;
                })
                .blockingGet();
        List<Concert> allConcert = dbHelper.selectByCondition(
                entityRegistry.concert,
                ConcertsTable.COLUMN_ARTIST_ID + "=?"
                        + " AND " + ConcertsTable.COLUMN_LOCATION_ID + "=?",
                artist.getId(),
                location.getId()
        );

        Date currentDate = beginOfDay(currentTime);
        Date maxKeepingDate = addDays(currentDate, -PAST_CONCERT_KEEPING_PERIOD_IN_DAYS);
        List<Concert> concertsToDelete = Observable.fromIterable(allConcert)
                .filter(concert -> {
                    Date concertDate = beginOfDay(concert.getDate());
                    boolean concertToday = concertDate.equals(currentDate);
                    boolean concertInFuture = concertDate.compareTo(currentDate) > 0;
                    boolean concertExists = loadedApiCodes.contains(concert.getApiCode());
                    boolean concertIsTooOld = concertDate.compareTo(maxKeepingDate) < 0;
                    boolean shouldKeepConcert;

                    if (concertToday) {
                        shouldKeepConcert = true;
                    } else if (concertInFuture) {
                        shouldKeepConcert = concertExists;
                    } else {
                        shouldKeepConcert = !concertIsTooOld;
                    }

                    return !shouldKeepConcert;
                })
                .toList()
                .blockingGet();
        concertRepository.blockingDeleteConcerts(concertsToDelete);

        return concertsToDelete;
    }

    private Date addDays(Date date, int days) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, days);

        return calendar.getTime();
    }

    private Date beginOfDay(Date date) {
        GregorianCalendar dateCal = new GregorianCalendar();
        dateCal.setTime(date);

        GregorianCalendar beginOfDayCal = new GregorianCalendar(
                dateCal.get(Calendar.YEAR),
                dateCal.get(Calendar.MONTH),
                dateCal.get(Calendar.DAY_OF_MONTH)
        );

        return beginOfDayCal.getTime();
    }

    private Observable<Concert> loadConcerts(Artist artist, Location location,
                                             ConcertService apiService) {
        String name = artist.getName().toLowerCase();

        return apiService.search(name, location.getApiCode())
                .flatMap(searchResponse -> Observable.fromIterable(searchResponse.getResults()))
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
