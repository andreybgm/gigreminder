package io.github.andreybgm.gigreminder.repository;

import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.github.andreybgm.gigreminder.data.Location;
import io.github.andreybgm.gigreminder.repository.api.MockingInterceptor;
import io.github.andreybgm.gigreminder.test.TestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(AndroidJUnit4.class)
public class LocationsRepositoryTest extends BaseRepositoryTest {
    private DataSource repository;
    private Location location1;
    private Location location2;
    private Location location3;
    private List<Location> locations;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        repository = new Repository(InstrumentationRegistry.getTargetContext());
        location1 = new Location("LC-1", "lc1", "Location1");
        location2 = new Location("LC-2", "lc2", "Location2");
        location3 = new Location("LC-3", "lc3", "Location3");
        locations = Arrays.asList(location1, location2, location3);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();

        MockingInterceptor.setShouldReturnError(false);
    }

    @Test
    public void saveLocation() throws Exception {
        List<Location> expected = Collections.singletonList(location1);

        repository.saveLocation(location1)
                .test()
                .await()
                .assertNoErrors()
                .assertComplete();

        repository.getLocations()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(actual -> TestUtils.assertListContainsAll(actual, expected));
    }

    @Test
    public void saveLocations() throws Exception {
        repository.saveLocations(locations)
                .test()
                .await()
                .assertNoErrors()
                .assertComplete();

        repository.getLocations()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(actual -> TestUtils.assertListContainsAll(actual, locations));
    }

    @Test
    public void getLocation() throws Exception {
        repository.saveLocation(location1).blockingAwait();

        repository.getLocation(location1.getId())
                .test()
                .await()
                .assertNoErrors()
                .assertValue(location1);
    }

    @Test
    public void deleteLocation() throws Exception {
        repository.saveLocation(location1).blockingAwait();

        repository.deleteLocation(location1)
                .test()
                .await()
                .assertComplete();

        repository.getLocations()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(Collections.emptyList());
    }

    @Test
    public void getAvailableLocations() throws Exception {
        repository.getAvailableLocations()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(actual -> TestUtils.assertListContainsAll(
                        actual, locations, this::compareIgnoringId));
    }

    @Test
    public void getAvailableLocationWhenSomeAlreadySaved() throws Exception {
        repository.saveLocations(Arrays.asList(location1, location2)).blockingAwait();

        repository.getAvailableLocations()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(locations -> {
                    assertThat(locations)
                            .hasSize(1)
                            .usingElementComparator(this::compareIgnoringId)
                            .containsOnly(location3);

                    return true;
                });
    }

    @Test
    public void getAvailableLocationsWhenCalledAgainShouldReturnCachedValue() throws Exception {
        List<Location> locations = repository.getAvailableLocations().blockingGet();
        MockingInterceptor.setShouldReturnError(true);

        repository.getAvailableLocations()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(actual -> TestUtils.assertListContainsAll(actual, locations));
    }

    @Test
    public void getAvailableLocationsWhenNetworkErrorShouldPass() throws Exception {
        MockingInterceptor.setShouldReturnError(true);

        repository.getAvailableLocations()
                .test()
                .await()
                .assertError(e -> true);
    }

    private int compareIgnoringId(@NonNull Location location1, @NonNull Location location2) {
        if (location1.equalsIgnoreId(location2)) {
            return 0;
        }

        return -1;
    }
}
