package io.github.andreybgm.gigreminder;

import android.support.annotation.NonNull;

import io.github.andreybgm.gigreminder.repository.api.MockingInterceptor;
import io.github.andreybgm.gigreminder.repository.artistsource.ArtistSource;
import io.github.andreybgm.gigreminder.repository.artistsource.FakeArtistSource;
import io.github.andreybgm.gigreminder.utils.FakeImageLoader;
import io.github.andreybgm.gigreminder.utils.ImageLoader;
import okhttp3.OkHttpClient;

public class Injection implements InjectionContract {

    private static final Injection injection = new Injection();

    @NonNull
    public static Injection provideInjection() {
        return injection;
    }

    @Override
    @NonNull
    public OkHttpClient provideOkHttpClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(MockingInterceptor.newInstance())
                .build();
    }

    @Override
    @NonNull
    public ArtistSource provideGoogleMusicSource() {
        return new FakeArtistSource();
    }

    @NonNull
    @Override
    public ImageLoader provideImageLoader() {
        return new FakeImageLoader();
    }
}
