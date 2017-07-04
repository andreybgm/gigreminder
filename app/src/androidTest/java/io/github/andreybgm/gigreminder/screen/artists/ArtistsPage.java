package io.github.andreybgm.gigreminder.screen.artists;

import android.support.test.espresso.matcher.ViewMatchers;

import io.github.andreybgm.gigreminder.R;
import io.github.andreybgm.gigreminder.test.PageObject;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;

public class ArtistsPage extends PageObject {

    public static ArtistsPage obtain() {
        return new ArtistsPage();
    }

    @Override
    public ArtistsPage assertOn() {
        assertIsDisplayed(R.id.screen_artists);

        return this;
    }

    public ArtistsPage clickNewArtist() {
        onView(ViewMatchers.withId(R.id.fab_add_artist)).perform(click());

        return this;
    }
}
