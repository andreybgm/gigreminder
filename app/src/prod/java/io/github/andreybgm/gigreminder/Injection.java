package io.github.andreybgm.gigreminder;

import android.support.annotation.NonNull;

import io.github.andreybgm.gigreminder.repository.artistsource.ArtistSource;
import io.github.andreybgm.gigreminder.repository.artistsource.GoogleMusicSource;
import io.github.andreybgm.gigreminder.utils.DefaultImageLoader;
import io.github.andreybgm.gigreminder.utils.ImageLoader;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import static okhttp3.logging.HttpLoggingInterceptor.Level;

public class Injection implements InjectionContract {

    private static final Injection injection = new Injection();

    @NonNull
    public static Injection provideInjection() {
        return injection;
    }

    @Override
    @NonNull
    public OkHttpClient provideOkHttpClient() {
        Level logLevel = BuildConfig.DEBUG ? Level.BASIC : Level.NONE;
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor().setLevel(logLevel);

        return new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();
    }

    @NonNull
    @Override
    public ArtistSource provideGoogleMusicSource() {
        return new GoogleMusicSource();
    }

    @NonNull
    @Override
    public ImageLoader provideImageLoader() {
        return new DefaultImageLoader();
    }
}
