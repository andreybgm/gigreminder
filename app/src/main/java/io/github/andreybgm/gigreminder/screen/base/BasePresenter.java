package io.github.andreybgm.gigreminder.screen.base;

import android.support.annotation.NonNull;

import io.github.andreybgm.gigreminder.repository.DataSource;
import io.github.andreybgm.gigreminder.utils.schedulers.SchedulerProvider;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public abstract class BasePresenter<UiModelT extends UiModel> {
    private Subject<UiEvent> uiEvents;
    private ConnectableObservable<UiModelT> uiModels;
    private final DataSource repository;
    private final SchedulerProvider schedulerProvider;

    public BasePresenter(@NonNull BasePresenterBuilder builder) {
        repository = builder.getRepository();
        schedulerProvider = builder.getSchedulerProvider();
    }

    public void connect() {
        uiEvents = PublishSubject.create();

        ObservableTransformer<UiEvent, UiModelT> eventToModelTransformer =
                events -> events
                        .compose(this::handleEvents)
                        .scan(getInitialUiModel(), this::reduceModel);

        uiModels = uiEvents
                .compose(eventToModelTransformer)
                .replay(1);
        uiModels.connect();
    }

    public Observable<UiModelT> getUiModels() {
        return uiModels;
    }

    public void sendUiEvent(UiEvent uiEvent) {
        uiEvents.onNext(uiEvent);
    }

    public DataSource getRepository() {
        return repository;
    }

    public SchedulerProvider getSchedulerProvider() {
        return schedulerProvider;
    }

    protected abstract UiModelT getInitialUiModel();

    protected abstract Observable<Result> handleEvents(Observable<UiEvent> events);

    protected abstract UiModelT reduceModel(UiModelT model, Result result);

    protected static <EventT> Observable<Result> handleEventsOfClass(
            Observable<UiEvent> events,
            Class<EventT> eventClass,
            ObservableTransformer<EventT, Result> eventHandler) {
        return events.ofType(eventClass).compose(eventHandler);
    }

    protected static RuntimeException makeUnknownResultException(Result result) {
        return new IllegalArgumentException("Unknown result " + result);
    }
}
