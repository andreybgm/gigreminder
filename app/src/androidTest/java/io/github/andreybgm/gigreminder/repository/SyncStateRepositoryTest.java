package io.github.andreybgm.gigreminder.repository;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.github.andreybgm.gigreminder.data.Artist;
import io.github.andreybgm.gigreminder.data.Location;
import io.github.andreybgm.gigreminder.data.SyncState;
import io.github.andreybgm.gigreminder.test.TestUtils;

@RunWith(AndroidJUnit4.class)
public class SyncStateRepositoryTest extends BaseRepositoryTest {

    private DataSource repository;
    private Artist artist1;
    private Artist artist2;
    private Location location1;
    private Location location2;
    private static final long RELEVANCE_PERIOD_HOURS = 12L;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        repository = RepositoryProvider.provideRepository(
                InstrumentationRegistry.getTargetContext());

        artist1 = new Artist("AR-1", "Artist1");
        artist2 = new Artist("AR-2", "Artist2");

        location1 = new Location("LC-1", "lc1", "Location1");
        location2 = new Location("LC-2", "lc2", "Location2");
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void addSyncStatesWhenSaveArtist() throws Exception {
        List<SyncState> expected = Arrays.asList(
                new SyncState(artist1, location1, new Date(0)),
                new SyncState(artist1, location2, new Date(0)));
        repository.saveLocations(Arrays.asList(location1, location2)).blockingAwait();

        repository.saveArtist(artist1).blockingAwait();

        repository.getSyncStates()
                .test()
                .await()
                .assertNoErrors()
                .assertValue(actual -> TestUtils.assertListContainsAll(actual, expected));
    }

    @Test
    public void resetSyncStateWhenUpdateArtist() throws Exception {
        SyncState syncState1 = new SyncState(artist1, location1, new Date(3600));
        SyncState expectedSyncState = new SyncState(artist1, location1, new Date(0));
        repository.saveLocation(location1).blockingAwait();
        repository.saveArtist(artist1).blockingAwait();

        repository.saveSyncState(syncState1).blockingAwait();
        repository.updateArtist(artist1).blockingAwait();

        repository.getSyncStates()
                .test()
                .await()
                .assertValue(Collections.singletonList(expectedSyncState));
    }

    @Test
    public void addSyncStatesWhenSaveLocation() throws Exception {
        List<SyncState> expected = Arrays.asList(
                new SyncState(artist1, location1, new Date(0)),
                new SyncState(artist2, location1, new Date(0)));
        repository.saveArtists(Arrays.asList(artist1, artist2)).blockingAwait();

        repository.saveLocation(location1).blockingAwait();

        repository.getSyncStates()
                .test()
                .await()
                .assertNoErrors()
                .assertValue(actual -> TestUtils.assertListContainsAll(actual, expected));
    }

    @Test
    public void getSyncStatesToUpdate() throws Exception {
        // given
        repository.saveArtists(Arrays.asList(artist1, artist2)).blockingAwait();
        repository.saveLocations(Arrays.asList(location1, location2)).blockingAwait();

        long nowHours = RELEVANCE_PERIOD_HOURS * 10;
        long earlierThanPeriodHours = nowHours - (RELEVANCE_PERIOD_HOURS * 2);
        long equalToPeriodHours = nowHours - RELEVANCE_PERIOD_HOURS;
        long laterThanPeriodHours = nowHours - 1;
        Date currentTime = new Date(TimeUnit.HOURS.toMillis(nowHours));

        SyncState emptyTimeSyncState = new SyncState(artist1, location1, new Date(0));
        SyncState earlierThanPeriodSyncState = new SyncState(artist2, location2,
                createDateByHours(earlierThanPeriodHours));
        SyncState equalToPeriodSyncState = new SyncState(artist2, location1,
                createDateByHours(equalToPeriodHours));
        SyncState laterThanPeriodSyncState = new SyncState(artist1, location2,
                createDateByHours(laterThanPeriodHours));

        List<SyncState> expected = Arrays.asList(
                emptyTimeSyncState, // it was saved when an artist and location saving
                earlierThanPeriodSyncState,
                equalToPeriodSyncState);

        repository.saveSyncState(laterThanPeriodSyncState).blockingAwait();
        repository.saveSyncState(equalToPeriodSyncState).blockingAwait();
        repository.saveSyncState(earlierThanPeriodSyncState).blockingAwait();

        // expected
        repository.getSyncStatesToUpdate(currentTime, RELEVANCE_PERIOD_HOURS)
                .test()
                .await()
                .assertNoErrors()
                .assertValue(actual -> TestUtils.assertListContainsAll(actual, expected));
    }

    @Test
    public void isSyncRequired() throws Exception {
        repository.saveLocation(location1).blockingAwait();
        repository.saveArtist(artist1).blockingAwait();

        repository.isSyncRequired(new Date(), RELEVANCE_PERIOD_HOURS)
                .test()
                .await()
                .assertNoErrors()
                .assertValue(true);
    }

    @Test
    public void isSyncRequiredWhenNoData() throws Exception {
        repository.isSyncRequired(new Date(), RELEVANCE_PERIOD_HOURS)
                .test()
                .await()
                .assertNoErrors()
                .assertValue(false);
    }

    @Test
    public void isSyncRequiredWhenAllUpdated() throws Exception {
        // given
        repository.saveLocation(location1).blockingAwait();
        repository.saveArtist(artist1).blockingAwait();

        long nowHours = RELEVANCE_PERIOD_HOURS * 2;
        Date recentSyncTime = createDateByHours(nowHours - 1);
        Date currentTime = createDateByHours(nowHours);
        SyncState newSyncState = new SyncState(artist1, location1, recentSyncTime);

        // action
        repository.saveSyncState(newSyncState).blockingAwait();

        // expected
        repository.isSyncRequired(currentTime,
                RELEVANCE_PERIOD_HOURS)
                .test()
                .await()
                .assertNoErrors()
                .assertValue(false);
    }

    @Test
    public void saveSyncState() throws Exception {
        SyncState syncState = new SyncState(artist1, location1, new Date(1000));

        repository.saveSyncState(syncState)
                .test()
                .await()
                .assertNoErrors()
                .assertComplete();
    }

    @Test
    public void saveSyncStateAgainShouldUpdateIt() throws Exception {
        // given
        repository.saveLocation(location1).blockingAwait();
        repository.saveArtist(artist1).blockingAwait();

        SyncState syncState1 = new SyncState(artist1, location1, new Date(1000));
        SyncState syncState2 = new SyncState(artist1, location1, new Date(2000));
        List<SyncState> expected = Collections.singletonList(syncState2);

        // action
        repository.saveSyncState(syncState1).blockingAwait();
        repository.saveSyncState(syncState2).blockingAwait();

        // expected
        repository.getSyncStates()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(actual -> TestUtils.assertListContainsAll(actual, expected));
    }

    private Date createDateByHours(long hours) {
        return new Date(TimeUnit.HOURS.toMillis(hours));
    }
}
