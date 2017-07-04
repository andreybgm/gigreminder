package io.github.andreybgm.gigreminder.repository.artistsource;

import android.content.Context;

import java.util.List;

public interface ArtistSource {
    List<String> loadArtists(Context context);
}
