package io.github.andreybgm.gigreminder;

import android.support.annotation.NonNull;

import io.github.andreybgm.gigreminder.repository.artistsource.ArtistSource;
import io.github.andreybgm.gigreminder.utils.ImageLoader;
import okhttp3.OkHttpClient;

public interface InjectionContract {
    @NonNull
    OkHttpClient provideOkHttpClient();

    @NonNull
    ArtistSource provideGoogleMusicSource();

    @NonNull
    ImageLoader provideImageLoader();
}
