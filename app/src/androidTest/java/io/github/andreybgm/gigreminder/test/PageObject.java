package io.github.andreybgm.gigreminder.test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public abstract class PageObject {

    public abstract PageObject assertOn();

    public void assertIsDisplayed(int id) {
        onView(withId(id)).check(matches(isDisplayed()));
    }

}
