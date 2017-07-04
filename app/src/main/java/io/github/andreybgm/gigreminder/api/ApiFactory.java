package io.github.andreybgm.gigreminder.api;

import io.github.andreybgm.gigreminder.BuildConfig;
import io.github.andreybgm.gigreminder.Injection;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiFactory {

    private static ConcertService concertService;

    private ApiFactory() {
    }

    public static synchronized ConcertService getConcertService() {
        if (concertService == null) {
            concertService = createRetrofit().create(ConcertService.class);
        }

        return concertService;
    }

    private static Retrofit createRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.API_ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(Injection.provideInjection().provideOkHttpClient())
                .build();
    }
}
