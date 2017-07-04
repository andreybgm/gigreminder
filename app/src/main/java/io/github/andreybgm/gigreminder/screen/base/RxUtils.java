package io.github.andreybgm.gigreminder.screen.base;

import io.github.andreybgm.gigreminder.utils.schedulers.DefaultSchedulerProvider;
import io.reactivex.ObservableTransformer;

public class RxUtils {

    private RxUtils() {
    }

    public static <T> ObservableTransformer<T, T> observeOnUi() {
        return source -> source
                .observeOn(DefaultSchedulerProvider.getInstance().ui());
    }
}
