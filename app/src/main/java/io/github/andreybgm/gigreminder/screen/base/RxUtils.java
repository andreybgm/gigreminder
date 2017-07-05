package io.github.andreybgm.gigreminder.screen.base;

import java.util.concurrent.TimeUnit;

import io.github.andreybgm.gigreminder.utils.schedulers.DefaultSchedulerProvider;
import io.reactivex.ObservableTransformer;

public class RxUtils {

    private static final int DEBOUNCE_TIMEOUT = 100;
    private static final TimeUnit DEBOUNCE_TIME_UNIT = TimeUnit.MILLISECONDS;

    private RxUtils() {
    }

    public static <T> ObservableTransformer<T, T> observeOnUiWithDebounce() {
        return source -> source
                .publish(shared -> shared.take(1)
                        .concatWith(shared.debounce(DEBOUNCE_TIMEOUT, DEBOUNCE_TIME_UNIT))
                )
                .observeOn(DefaultSchedulerProvider.getInstance().ui());
    }
}
