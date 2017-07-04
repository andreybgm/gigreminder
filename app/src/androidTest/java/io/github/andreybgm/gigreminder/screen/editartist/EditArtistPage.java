package io.github.andreybgm.gigreminder.screen.editartist;

import io.github.andreybgm.gigreminder.R;
import io.github.andreybgm.gigreminder.test.PageObject;

public class EditArtistPage extends PageObject {

    public static EditArtistPage obtain() {
        return new EditArtistPage();
    }

    @Override
    public EditArtistPage assertOn() {
        assertIsDisplayed(R.id.screen_edit_artist);

        return this;
    }
}
