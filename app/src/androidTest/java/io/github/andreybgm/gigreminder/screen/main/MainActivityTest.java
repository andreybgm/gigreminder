package io.github.andreybgm.gigreminder.screen.main;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.andreybgm.gigreminder.screen.artists.ArtistsPage;
import io.github.andreybgm.gigreminder.screen.editartist.EditArtistPage;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(
            MainActivity.class);

    @Test
    public void shouldOpenNewArtistScreen() throws Exception {
        MainPage.obtain()
                .assertOn()
                .openArtists();

        ArtistsPage.obtain()
                .assertOn()
                .clickNewArtist();

        EditArtistPage.obtain()
                .assertOn();
    }

}