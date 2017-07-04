package io.github.andreybgm.gigreminder.utils.schedulers;

import io.reactivex.Scheduler;

public interface SchedulerProvider {
    Scheduler ui();

    Scheduler io();

    Scheduler computation();
}
