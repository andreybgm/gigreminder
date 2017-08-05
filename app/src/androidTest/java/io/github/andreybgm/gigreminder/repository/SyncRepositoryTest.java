package io.github.andreybgm.gigreminder.repository;

import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import io.github.andreybgm.gigreminder.data.Artist;
import io.github.andreybgm.gigreminder.data.Concert;
import io.github.andreybgm.gigreminder.data.Location;
import io.github.andreybgm.gigreminder.eventbus.BaseEvent;
import io.github.andreybgm.gigreminder.repository.entityrepository.SyncRepository;
import io.github.andreybgm.gigreminder.repository.sync.AppSyncResult;
import io.github.andreybgm.gigreminder.repository.sync.eventbus.SyncEventBus;
import io.github.andreybgm.gigreminder.repository.sync.eventbus.SyncFinishEvent;
import io.github.andreybgm.gigreminder.repository.sync.eventbus.SyncStartEvent;
import io.reactivex.Single;
import io.reactivex.observables.ConnectableObservable;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(AndroidJUnit4.class)
public class SyncRepositoryTest extends BaseRepositoryTest {

    private static final long MAX_TIMEOUT_MILLIS = 3000L;
    private static final int RELEVANCE_PERIOD_HOURS = 12;
    private static final Date CURRENT_TIME = new GregorianCalendar(2016, 11, 31).getTime();

    private DataSource repository;
    private Artist artist1;
    private Artist artist2;
    private Artist overlapNameArtist;
    private Location location1;
    private Location location2;
    private Concert concertArtist1Location1;
    private Concert concertArtist1Location1Version0;
    private Concert concertArtist1Location2Version0;
    private Concert concertArtist1Location2;
    private ConnectableObservable<BaseEvent> syncEventBus;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        repository = RepositoryProvider.provideRepository(
                InstrumentationRegistry.getTargetContext());

        artist1 = new Artist("AR-1", "Artist1");
        artist2 = new Artist("AR-2", "Artist2");
        // the full name is "Triple artist name"
        overlapNameArtist = new Artist("AR-3", "Triple artist");

        location1 = new Location("LC-1", "lc1", "Location1");
        location2 = new Location("LC-2", "lc2", "Location2");

        concertArtist1Location1Version0 = new Concert.Builder("CN-1-1", "1001", artist1, location1)
                .date((new GregorianCalendar(2017, 0, 1, 0, 0)).getTime())
                .place("Place2000")
                .imageUrl("http://example.com/img1000.jpg")
                .url("http://example.com/events/1000")
                .build();
        concertArtist1Location1 = new Concert.Builder("CN-1-1", "1001", artist1, location1)
                .date((new GregorianCalendar(2017, 1, 1, 20, 30)).getTime())
                .place("Place2001")
                .imageUrl("http://example.com/img1001.jpg")
                .url("http://example.com/events/1001")
                .build();

        concertArtist1Location2Version0 = new Concert.Builder("CN-1-2", "1002", artist1, location2)
                .date((new GregorianCalendar(2017, 0, 1, 0, 0)).getTime())
                .place("Place2000")
                .imageUrl("http://example.com/img1000.jpg")
                .url("http://example.com/events/1000")
                .build();
        concertArtist1Location2 = new Concert.Builder("CN-1-2", "1002", artist1, location2)
                .date((new GregorianCalendar(2017, 1, 2, 20, 30)).getTime())
                .place("Place2002")
                .imageUrl("http://example.com/img1002.jpg")
                .url("http://example.com/events/1002")
                .build();

        syncEventBus = SyncEventBus.getEventBus()
                .replay(2);
        syncEventBus.connect();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void syncDataWhenOneNewConcert() throws Exception {
        // given
        repository.saveArtist(artist1).blockingAwait();
        repository.saveLocation(location1).blockingAwait();

        // action
        Single<AppSyncResult> syncResultSingle =
                repository.syncData(CURRENT_TIME, RELEVANCE_PERIOD_HOURS);

        // check
        syncResultSingle
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(syncResult -> {
                    List<Concert> newConcerts = syncResult.getNewConcerts();
                    assertThat(newConcerts)
                            .hasSize(1)
                            .usingElementComparator(this::compareIgnoringId)
                            .containsOnly(concertArtist1Location1);

                    return true;
                });

        repository.getConcerts()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(actualConcerts -> {
                    assertThat(actualConcerts)
                            .hasSize(1)
                            .usingElementComparator(this::compareIgnoringId)
                            .containsOnly(concertArtist1Location1);

                    return true;
                });

        repository.getSyncStatesToUpdate(CURRENT_TIME, RELEVANCE_PERIOD_HOURS)
                .test()
                .await()
                .assertNoErrors()
                .assertValue(Collections.emptyList());

        checkSyncStartAndFinishEvents(syncEventBus);
    }

    @Test
    public void syncDataWhenArtistNameOverlap() throws Exception {
        repository.saveArtist(overlapNameArtist).blockingAwait();
        repository.saveLocation(location1).blockingAwait();

        repository.syncData(CURRENT_TIME, RELEVANCE_PERIOD_HOURS)
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(syncResult -> {
                    assertThat(syncResult.getNewConcerts()).isEmpty();

                    return true;
                });

        repository.getConcerts()
                .test()
                .awaitCount(1)
                .assertValue(Collections.emptyList());
    }

    @Test
    public void syncDataWhenTwoConcertExistsShouldUpdateThem() throws Exception {
        repository.saveArtist(artist1).blockingAwait();
        repository.saveLocations(Arrays.asList(location1, location2)).blockingAwait();
        repository.saveConcerts(
                Arrays.asList(concertArtist1Location1Version0, concertArtist1Location2Version0))
                .blockingAwait();

        repository.syncData(CURRENT_TIME, RELEVANCE_PERIOD_HOURS)
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(syncResult -> {
                    assertThat(syncResult.getNewConcerts()).isEmpty();

                    return true;
                });

        repository.getConcerts()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(actualConcerts -> {
                    assertThat(actualConcerts)
                            .hasSize(2)
                            .containsOnly(concertArtist1Location1, concertArtist1Location2);

                    return true;
                });
    }

    @Test
    public void cleanConcertsWhenSync() throws Exception {
        Concert todayConcert = new Concert.Builder("TODAY", "9001", artist2, location2)
                .date(CURRENT_TIME)
                .place("Place2001")
                .imageUrl("http://example.com/img1001.jpg")
                .url("http://example.com/events/1005")
                .build();

        GregorianCalendar past = new GregorianCalendar();
        past.setTime(CURRENT_TIME);
        past.add(Calendar.DAY_OF_MONTH, -SyncRepository.PAST_CONCERT_KEEPING_PERIOD_IN_DAYS);
        Concert pastConcert = new Concert.Builder("PAST", "9002", artist2, location2)
                .date(past.getTime())
                .place("Place2001")
                .imageUrl("http://example.com/img1001.jpg")
                .url("http://example.com/events/1005")
                .build();

        GregorianCalendar tooOldDate = new GregorianCalendar();
        tooOldDate.setTime(CURRENT_TIME);
        tooOldDate.add(Calendar.DAY_OF_MONTH,
                -(SyncRepository.PAST_CONCERT_KEEPING_PERIOD_IN_DAYS + 1));
        Concert tooOldConcert = new Concert.Builder("TOO_OLD", "9003", artist2, location2)
                .date(tooOldDate.getTime())
                .place("Place2001")
                .imageUrl("http://example.com/img1001.jpg")
                .url("http://example.com/events/1005")
                .build();

        GregorianCalendar tomorrow = new GregorianCalendar();
        tomorrow.setTime(CURRENT_TIME);
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        Concert nonexistentConcert = new Concert.Builder("NONEXISTENT", "9004", artist2, location2)
                .date(tomorrow.getTime())
                .place("Place2001")
                .imageUrl("http://example.com/img1001.jpg")
                .url("http://example.com/events/1005")
                .build();

        // today and past (during the keeping period) concerts should always stay
        // the too old past concert should be deleted
        // the future concert should be deleted if it doesn't exist on the server
        repository.saveArtist(artist2).blockingAwait();
        repository.saveLocation(location2).blockingAwait();
        repository.saveConcerts(Arrays.asList(
                todayConcert, pastConcert, tooOldConcert, nonexistentConcert))
                .blockingAwait();

        repository.syncData(CURRENT_TIME, RELEVANCE_PERIOD_HOURS).blockingGet();

        repository.getConcerts()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(actualConcerts -> {
                    assertThat(actualConcerts)
                            .hasSize(2)
                            .containsOnly(todayConcert, pastConcert);

                    return true;
                });
    }

    @Test
    public void syncDataWithNetworkError() throws Exception {
        repository.saveArtist(artist1).blockingAwait();
        repository.saveLocation(new Location("lc0", "Nonexistent location")).blockingAwait();

        repository.syncData(CURRENT_TIME, RELEVANCE_PERIOD_HOURS)
                .test()
                .await()
                .assertError(e -> true);
        checkSyncStartAndFinishEvents(syncEventBus);
    }

    @Test
    public void onSyncInterrupted() throws Exception {
        repository.onSyncInterrupted();

        syncEventBus
                .test()
                .awaitCount(1, () -> {
                }, MAX_TIMEOUT_MILLIS)
                .assertNoErrors()
                .assertValueAt(1, event -> {
                    assertThat(event).isInstanceOf(SyncFinishEvent.class);
                    return true;
                });
    }

    private void checkSyncStartAndFinishEvents(ConnectableObservable<BaseEvent> syncEventBus) {
        syncEventBus
                .test()
                .awaitCount(2, () -> {
                }, MAX_TIMEOUT_MILLIS)
                .assertNoErrors()
                .assertValueAt(0, event -> {
                    assertThat(event).isInstanceOf(SyncStartEvent.class);
                    return true;
                })
                .assertValueAt(1, event -> {
                    assertThat(event).isInstanceOf(SyncFinishEvent.class);
                    return true;
                });
    }

    private int compareIgnoringId(@NonNull Concert concert1, @NonNull Concert concert2) {
        if (concert1.equalsIgnoreId(concert2)) {
            return 0;
        }

        return -1;
    }
}
