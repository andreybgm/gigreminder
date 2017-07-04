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
import io.github.andreybgm.gigreminder.test.TestUtils;

@RunWith(AndroidJUnit4.class)
public class ConcertsRepositoryTest extends BaseRepositoryTest {
    private DataSource repository;
    private Concert artist1Location1Concert;
    private Concert artist1Location2Concert;
    private Concert artist1Location1ConcertVersion0;
    private Artist artist1;
    private Artist artist2;
    private Location location1;
    private Location location2;
    private Concert artist1Location2ConcertVersion0;
    private List<Concert> concerts;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        repository = new Repository(InstrumentationRegistry.getTargetContext());

        artist1 = new Artist("AR-1", "Artist1");
        artist2 = new Artist("AR-2", "Artist2");

        location1 = new Location("LC-1", "lc1", "Location1");
        location2 = new Location("LC-2", "lc2", "Location2");

        repository.saveArtists(Arrays.asList(artist1, artist2)).blockingAwait();
        repository.saveLocations(Arrays.asList(location1, location2)).blockingAwait();

        artist1Location1ConcertVersion0 = new Concert.Builder("CN-1-1", "1001", artist1, location1)
                .date((new GregorianCalendar(2017, 0, 1, 20, 30)).getTime())
                .place("Place2000")
                .imageUrl("http://github.com/img1000.jpg")
                .url("http://github.com/events/1000")
                .build();
        artist1Location1Concert = new Concert.Builder("CN-1-1", "1001", artist1, location1)
                .date((new GregorianCalendar(2017, 1, 1, 20, 30)).getTime())
                .place("Place2001")
                .imageUrl("http://github.com/img1001.jpg")
                .url("http://github.com/events/1001")
                .build();

        artist1Location2ConcertVersion0 = new Concert.Builder("CN-1-2", "1002", artist1, location2)
                .date((new GregorianCalendar(2017, 0, 1, 20, 30)).getTime())
                .place("Place2000")
                .imageUrl("http://github.com/img1000.jpg")
                .url("http://github.com/events/1000")
                .build();
        artist1Location2Concert = new Concert.Builder("CN-1-2", "1002", artist1, location2)
                .date((new GregorianCalendar(2017, 1, 1, 20, 30)).getTime())
                .place("Place2002")
                .imageUrl("http://github.com/img1002.jpg")
                .url("http://github.com/events/1002")
                .build();

        concerts = Arrays.asList(artist1Location1Concert, artist1Location2Concert);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void saveConcert() throws Exception {
        List<Concert> expected = Collections.singletonList(artist1Location1Concert);

        repository.saveConcert(artist1Location1Concert)
                .test()
                .await()
                .assertNoErrors()
                .assertComplete();

        repository.getConcerts()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(actual -> TestUtils.assertListContainsAll(actual, expected));
    }

    @Test
    public void saveAlreadyExistedConcertsShouldUpdateIt() throws Exception {
        repository.saveConcerts(Arrays.asList(
                artist1Location1ConcertVersion0, artist1Location2ConcertVersion0))
                .blockingAwait();

        repository.saveConcerts(Arrays.asList(artist1Location1Concert, artist1Location2Concert))
                .test()
                .await()
                .assertNoErrors()
                .assertComplete();

        repository.getConcerts()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(actualConcerts -> TestUtils.assertListContainsAll(
                        actualConcerts, concerts));
    }

    @Test
    public void saveConcerts() throws Exception {
        repository.saveConcerts(concerts)
                .test()
                .await()
                .assertNoErrors()
                .assertComplete();

        repository.getConcerts()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(actualConcerts -> TestUtils.assertListContainsAll(
                        actualConcerts, concerts));
    }

    @Test
    public void getConcert() throws Exception {
        repository.saveConcert(artist1Location1Concert).blockingAwait();

        repository.getConcert(artist1Location1Concert.getId())
                .test()
                .await()
                .assertNoErrors()
                .assertValue(artist1Location1Concert);
    }

    @Test
    public void deleteConcert() throws Exception {
        repository.saveConcert(artist1Location1Concert).blockingAwait();

        repository.deleteConcert(artist1Location1Concert)
                .test()
                .await()
                .assertNoErrors()
                .assertComplete();

        repository.getConcerts()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(Collections.emptyList());
    }
}
