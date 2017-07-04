package io.github.andreybgm.gigreminder.repository;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.github.andreybgm.gigreminder.data.Artist;
import io.github.andreybgm.gigreminder.repository.artistsource.FakeArtistSource;
import io.github.andreybgm.gigreminder.repository.error.DataNotFoundException;
import io.github.andreybgm.gigreminder.repository.error.NotUniqueArtistException;
import io.github.andreybgm.gigreminder.test.TestUtils;
import io.reactivex.Observable;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(AndroidJUnit4.class)
public class ArtistsRepositoryTest extends BaseRepositoryTest {
    private DataSource repository;

    private List<Artist> artists;
    private Artist artist1;
    private Artist artist2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        repository = new Repository(InstrumentationRegistry.getTargetContext());
        artist1 = new Artist("artist 1");
        artist2 = new Artist("artist 2");
        Artist artist = new Artist("artist 3");
        artists = Arrays.asList(artist1, artist2, artist);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void saveArtist() throws Exception {
        List<Artist> expected = Collections.singletonList(artist1);

        repository.saveArtist(artist1)
                .test()
                .await()
                .assertComplete();

        repository.getArtists()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(actual -> TestUtils.assertListContainsAll(actual, expected));
    }

    @Test
    public void saveArtists() throws Exception {
        repository.saveArtists(artists)
                .test()
                .await()
                .assertNoErrors()
                .assertComplete();

        repository.getArtists()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(actual -> TestUtils.assertListContainsAll(actual, artists));
    }

    @Test
    public void saveArtistWhenItIsNotUniqueShouldFail() throws Exception {
        repository.saveArtist(artist1).blockingAwait();
        Artist newArtist = new Artist(artist1.getName());

        repository.saveArtist(newArtist)
                .test()
                .await()
                .assertError(NotUniqueArtistException.class);
    }

    @Test
    public void updateArtist() throws Exception {
        String newName = "new name";
        Artist updatedArtist = new Artist(artist1.getId(), newName);
        repository.saveArtist(artist1).blockingAwait();

        repository.updateArtist(updatedArtist)
                .test()
                .await()
                .assertComplete();

        repository.getArtists()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(actualArtists -> {
                    assertThat(actualArtists)
                            .hasSize(1)
                            .containsOnly(updatedArtist);

                    return true;
                });
    }

    @Test
    public void updateArtistWhenItIsNotUniqueShouldFail() throws Exception {
        Artist updatedArtist = new Artist(artist1.getId(), artist2.getName());
        repository.saveArtists(Arrays.asList(artist1, artist2)).blockingAwait();

        repository.updateArtist(updatedArtist)
                .test()
                .await()
                .assertError(NotUniqueArtistException.class);
    }

    @Test
    public void updateArtistWhenItIsNotPresentShouldFail() throws Exception {
        repository.updateArtist(artist1)
                .test()
                .await()
                .assertError(DataNotFoundException.class);
    }

    @Test
    public void getArtist() throws Exception {
        repository.saveArtist(artist1).blockingAwait();

        repository.getArtist(artist1.getId())
                .test()
                .await()
                .assertNoErrors()
                .assertValue(artist1);
    }

    @Test
    public void getArtistByIdWhenItIsNotFoundShouldFail() throws Exception {
        repository.getArtist(artist1.getId())
                .test()
                .await()
                .assertError(DataNotFoundException.class);
    }

    @Test
    public void deleteArtist() throws Exception {
        repository.saveArtist(artist1).blockingAwait();

        repository.deleteArtist(artist1)
                .test()
                .await()
                .assertNoErrors()
                .assertComplete();

        assertThereAreNoArtistsInDataSource(repository);
    }

    @Test
    public void deleteArtists() throws Exception {
        repository.saveArtists(artists).blockingAwait();

        repository.deleteArtists(artists)
                .test()
                .await()
                .assertNoErrors()
                .assertComplete();

        assertThereAreNoArtistsInDataSource(repository);
    }

    @Test
    public void loadFromGoogleMusic() throws Exception {
        repository.loadArtistsFromGoogleMusic()
                .test()
                .await()
                .assertNoErrors()
                .assertValue(artists -> {
                    assertThat(artists).hasSize(FakeArtistSource.ARTIST_COUNT);

                    return true;
                });
    }

    @Test
    public void loadFromGoogleMusicWhenArtistExists() throws Exception {
        // given: save few artists
        final int savedCount = 3;
        final int expectedNameFirstIndex = savedCount + 1;
        final int expectedNameCount = FakeArtistSource.ARTIST_COUNT - savedCount;

        //noinspection ConstantConditions
        if (expectedNameCount <= 0) {
            throw new IllegalStateException("Must not be 0");
        }

        List<Artist> savedArtists = Observable.range(1, savedCount)
                .map(FakeArtistSource::generateName)
                .map(Artist::new)
                .toList()
                .blockingGet();
        repository.saveArtists(savedArtists).blockingAwait();

        // expect: only not saved artist's names should be loaded
        List<String> expectedNames = Observable.range(
                expectedNameFirstIndex, expectedNameCount)
                .map(FakeArtistSource::generateName)
                .toList()
                .blockingGet();

        repository.loadArtistsFromGoogleMusic()
                .test()
                .await()
                .assertNoErrors()
                .assertValue(artists -> {
                    assertThat(artists)
                            .hasSize(expectedNames.size())
                            .containsAll(expectedNames);

                    return true;
                });
    }

    private void assertThereAreNoArtistsInDataSource(DataSource dataSource) {
        dataSource.getArtists()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(Collections.emptyList());
    }
}