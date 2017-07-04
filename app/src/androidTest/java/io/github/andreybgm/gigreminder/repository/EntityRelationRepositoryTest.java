package io.github.andreybgm.gigreminder.repository;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import io.github.andreybgm.gigreminder.data.Artist;
import io.github.andreybgm.gigreminder.data.Concert;
import io.github.andreybgm.gigreminder.data.Location;
import io.github.andreybgm.gigreminder.data.SyncState;
import io.github.andreybgm.gigreminder.test.TestUtils;

@RunWith(AndroidJUnit4.class)
public class EntityRelationRepositoryTest extends BaseRepositoryTest {
    private DataSource repository;
    private Concert concertArtist1Location1;
    private Concert concertArtist1Location2;
    private Concert concertArtist2Location1;
    private Concert concertArtist2Location2;
    private Artist artist1;
    private Artist artist2;
    private Location location1;
    private Location location2;
    private SyncState syncStateArtitst1Location1;
    private SyncState syncStateArtitst1Location2;
    private SyncState syncStateArtitst2Location1;
    private SyncState syncStateArtitst2Location2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        repository = new Repository(InstrumentationRegistry.getTargetContext());

        artist1 = new Artist("Artist1");
        artist2 = new Artist("Artist2");

        location1 = new Location("cd1", "Location1");
        location2 = new Location("cd2", "Location2");

        concertArtist1Location1 = new Concert.Builder("XXX-FF-YYY-11", "11", artist1,
                location1)
                .date((new GregorianCalendar(2017, 1, 1, 20, 30)).getTime())
                .place("place11")
                .imageUrl("http://github.com/img11.jpg")
                .url("http://github.com/events/11")
                .build();
        concertArtist1Location2 = new Concert.Builder("XXX-FF-YYY-12", "12", artist1,
                location2)
                .date((new GregorianCalendar(2017, 1, 2, 20, 0)).getTime())
                .place("place12")
                .imageUrl("http://github.com/img12.jpg")
                .url("http://github.com/events/12")
                .build();
        concertArtist2Location1 = new Concert.Builder("XXX-FF-YYY-21", "21", artist2,
                location1)
                .date((new GregorianCalendar(2017, 2, 1, 20, 30)).getTime())
                .place("place21")
                .imageUrl("http://github.com/img21.jpg")
                .url("http://github.com/events/21")
                .build();
        concertArtist2Location2 = new Concert.Builder("XXX-FF-YYY-22", "22", artist2,
                location2)
                .date((new GregorianCalendar(2017, 2, 2, 20, 0)).getTime())
                .place("place22")
                .imageUrl("http://github.com/img22.jpg")
                .url("http://github.com/events/22")
                .build();

        syncStateArtitst1Location1 = new SyncState(artist1, location1);
        syncStateArtitst1Location2 = new SyncState(artist1, location2);
        syncStateArtitst2Location1 = new SyncState(artist2, location1);
        syncStateArtitst2Location2 = new SyncState(artist2, location2);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void whenDeleteLocationThenConcertShouldBeDeleted() throws Exception {
        List<Concert> expected = Arrays.asList(
                concertArtist1Location2, concertArtist2Location2);
        saveAllData();

        repository.deleteLocation(location1).blockingAwait();

        repository.getConcerts()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(actual -> TestUtils.assertListContainsAll(actual, expected));
    }

    @Test
    public void whenDeleteLocationThenSyncStatesShouldBeDeleted() throws Exception {
        List<SyncState> expected = Arrays.asList(
                syncStateArtitst1Location2, syncStateArtitst2Location2);
        saveAllData();

        repository.deleteLocation(location1).blockingAwait();

        repository.getSyncStates()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(actual -> TestUtils.assertListContainsAll(actual, expected));
    }

    @Test
    public void whenDeleteArtistThenConcertShouldBeDeleted() throws Exception {
        List<Concert> expected = Arrays.asList(
                concertArtist2Location1, concertArtist2Location2);
        saveAllData();

        repository.deleteArtist(artist1).blockingAwait();

        repository.getConcerts()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(actual -> TestUtils.assertListContainsAll(actual, expected));
    }

    @Test
    public void whenDeleteArtistThenSyncStatesShouldBeDeleted() throws Exception {
        List<SyncState> expected = Arrays.asList(
                syncStateArtitst2Location1, syncStateArtitst2Location2);
        saveAllData();

        repository.deleteArtist(artist1).blockingAwait();

        repository.getSyncStates()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(actual -> TestUtils.assertListContainsAll(actual, expected));
    }

    @Test
    public void whenArtistDoesNotExistThenConcertShouldNotBeSaved() throws Exception {
        repository.saveLocation(location1).blockingAwait();

        repository.saveConcert(concertArtist1Location1).blockingAwait();

        repository.getConcerts()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(Collections.emptyList());
    }

    @Test
    public void whenLocationDoesNotExistThenConcertShouldNotBeSaved() throws Exception {
        repository.saveArtist(artist1).blockingAwait();

        repository.saveConcert(concertArtist1Location1).blockingAwait();

        repository.getConcerts()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(Collections.emptyList());
    }

    private void saveAllData() {
        repository.saveArtists(Arrays.asList(artist1, artist2)).blockingAwait();
        repository.saveLocations(Arrays.asList(location1, location2)).blockingAwait();
        repository.saveConcerts(Arrays.asList(concertArtist1Location1, concertArtist1Location2,
                concertArtist2Location1, concertArtist2Location2)).blockingAwait();
    }
}
