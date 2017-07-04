package io.github.andreybgm.gigreminder.utils.schedulers;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DefaultSchedulerProvider implements SchedulerProvider {

    private static DefaultSchedulerProvider instance = new DefaultSchedulerProvider();

    private DefaultSchedulerProvider() {

    }

    public static synchronized DefaultSchedulerProvider getInstance() {
        return instance;
    }

    @Override
    public Scheduler ui() {
        return AndroidSchedulers.mainThread();
    }

    @Override
    public Scheduler io() {
        return Schedulers.io();
    }

    @Override
    public Scheduler computation() {
        return Schedulers.computation();
    }
}
