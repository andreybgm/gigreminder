package io.github.andreybgm.gigreminder.screen.main;

import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.matcher.ViewMatchers;

import io.github.andreybgm.gigreminder.R;
import io.github.andreybgm.gigreminder.test.PageObject;

import static android.support.test.espresso.Espresso.onView;

public class MainPage extends PageObject {

    public static MainPage obtain() {
        return new MainPage();
    }

    @Override
    public MainPage assertOn() {
        assertIsDisplayed(R.id.screen_main);

        return this;
    }

    public MainPage openArtists() {
        onView(ViewMatchers.withId(R.id.pager)).perform(ViewActions.swipeLeft());

        return this;
    }
}
