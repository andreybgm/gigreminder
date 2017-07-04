package io.github.andreybgm.gigreminder.screen.concerts;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import io.github.andreybgm.gigreminder.data.Concert;
import io.github.andreybgm.gigreminder.repository.DataSource;
import io.github.andreybgm.gigreminder.repository.sync.eventbus.SyncEventBus;
import io.github.andreybgm.gigreminder.repository.sync.eventbus.SyncFinishEvent;
import io.github.andreybgm.gigreminder.repository.sync.eventbus.SyncStartEvent;
import io.github.andreybgm.gigreminder.screen.base.BasePresenter;
import io.github.andreybgm.gigreminder.screen.base.BasePresenterBuilder;
import io.github.andreybgm.gigreminder.screen.base.Result;
import io.github.andreybgm.gigreminder.screen.base.UiEvent;
import io.github.andreybgm.gigreminder.screen.concerts.uievent.ConcertClickEvent;
import io.github.andreybgm.gigreminder.screen.concerts.uievent.LoadConcertsErrorConfirmEvent;
import io.github.andreybgm.gigreminder.screen.concerts.uievent.LoadConcertsEvent;
import io.github.andreybgm.gigreminder.screen.concerts.uievent.LoadConcertsResult;
import io.github.andreybgm.gigreminder.screen.concerts.uievent.OpenConcertConfirmEvent;
import io.github.andreybgm.gigreminder.screen.concerts.uievent.SyncResult;
import io.github.andreybgm.gigreminder.utils.schedulers.SchedulerProvider;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;

public class ConcertsPresenter extends BasePresenter<ConcertsUiModel> {

    private final ConcertsUiModel initialUiModel;
    private final CurrentTimeProvider currentTimeProvider;

    public ConcertsPresenter(Builder builder) {
        super(builder);

        initialUiModel = makeInitialUiModel(builder);
        currentTimeProvider = builder.currentTimeProvider;

        connect();
    }

    @Override
    protected ConcertsUiModel getInitialUiModel() {
        return initialUiModel;
    }

    @Override
    protected Observable<Result> handleEvents(Observable<UiEvent> events) {
        ObservableTransformer<LoadConcertsEvent, Result> loadConcerts = loadEvents ->
                loadEvents.flatMap(event ->
                        getRepository().getConcerts()
                                .map(LoadConcertsResult::success)
                                .onErrorReturn(LoadConcertsResult::error)
                                .startWith(LoadConcertsResult.IN_PROGRESS)
                );

        Observable<Result> syncing = SyncEventBus.getEventBus()
                .flatMap(busEvent -> {
                    if (busEvent instanceof SyncStartEvent) {
                        return Observable.just(SyncResult.create(true));
                    } else if (busEvent instanceof SyncFinishEvent) {
                        return Observable.just(SyncResult.create(false));
                    }

                    return Observable.empty();
                });

        return events.publish(sharedEvents -> Observable.merge(
                handleEventsOfClass(sharedEvents, LoadConcertsEvent.class, loadConcerts),
                syncing,
                sharedEvents.ofType(Result.class)
        ));
    }

    @Override
    protected ConcertsUiModel reduceModel(ConcertsUiModel model, Result result) {
        if (result instanceof LoadConcertsResult) {
            return reduceModel(model, (LoadConcertsResult) result);
        } else if (result instanceof LoadConcertsErrorConfirmEvent) {
            return reduceModel(model, (LoadConcertsErrorConfirmEvent) result);
        } else if (result instanceof ConcertClickEvent) {
            return reduceModel(model, (ConcertClickEvent) result);
        } else if (result instanceof OpenConcertConfirmEvent) {
            return reduceModel(model, (OpenConcertConfirmEvent) result);
        } else if (result instanceof SyncResult) {
            return reduceModel(model, (SyncResult) result);
        }

        throw makeUnknownResultException(result);
    }

    private ConcertsUiModel reduceModel(ConcertsUiModel model, LoadConcertsResult result) {
        if (result.isInProgress()) {
            return model.copy()
                    .loading(true)
                    .build();
        } else if (result.isSuccess()) {
            List<Concert> concerts = sortConcerts(result.getConcerts(), currentTimeProvider);

            return model.copy()
                    .loading(false)
                    .loadingError(false)
                    .concerts(concerts)
                    .build();
        } else if (result.isError()) {
            return model.copy()
                    .loading(false)
                    .loadingError(true)
                    .concerts(Collections.emptyList())
                    .build();
        }

        throw makeUnknownResultException(result);
    }

    private ConcertsUiModel reduceModel(ConcertsUiModel model,
                                        LoadConcertsErrorConfirmEvent result) {
        return model.copy()
                .loadingError(false)
                .build();
    }

    private ConcertsUiModel reduceModel(ConcertsUiModel model, ConcertClickEvent result) {
        int position = result.getPosition();
        int maxPosition = model.getConcerts().size() - 1;

        if (position > maxPosition) {
            return model;
        }

        Concert concert = model.getConcerts().get(position);

        return model.copy()
                .concertToOpen(concert)
                .build();
    }

    private ConcertsUiModel reduceModel(ConcertsUiModel model, OpenConcertConfirmEvent result) {
        return model.copy()
                .clearConcertToOpen()
                .build();
    }

    private ConcertsUiModel reduceModel(ConcertsUiModel model, SyncResult result) {
        return model.copy()
                .syncing(result.isSyncing())
                .build();
    }

    private static ConcertsUiModel makeInitialUiModel(Builder builder) {
        if (builder.getUiModel().isPresent()) {
            return builder.getUiModel().getValue();
        }

        return ConcertsUiModel.DEFAULT;
    }

    private static List<Concert> sortConcerts(List<Concert> concerts,
                                              CurrentTimeProvider currentTimeProvider) {
        GregorianCalendar currentTime = new GregorianCalendar();
        currentTime.setTime(currentTimeProvider.currentTime());

        Date currentDay = new GregorianCalendar(
                currentTime.get(Calendar.YEAR),
                currentTime.get(Calendar.MONTH),
                currentTime.get(Calendar.DATE))
                .getTime();

        List<Concert> sortedConcerts = new ArrayList<>(concerts);
        Collections.sort(sortedConcerts, (concert1, concert2) -> {
            // wanted to sort:
            //   current date -> artist -> location -> id
            //   future date -> artist -> location -> id
            //   past date -> artist -> location -> id
            Date date1 = concert1.getDate();
            Date date2 = concert2.getDate();

            boolean date1IsPast = date1.compareTo(currentDay) < 0;
            boolean date2IsPast = date2.compareTo(currentDay) < 0;

            if (date1IsPast && date2IsPast) {
                return compareConcerts(concert1, concert2);
            } else if (date1IsPast) {
                return 1; // the first concert goes to the end
            } else if (date2IsPast) {
                return -1; // the first concert goes to the beginning
            }

            return compareConcerts(concert1, concert2);
        });

        return sortedConcerts;
    }

    private static int compareConcerts(Concert concert1, Concert concert2) {
        Date date1 = concert1.getDate();
        Date date2 = concert2.getDate();
        int dateResult = date1.compareTo(date2);

        if (dateResult != 0) {
            return dateResult;
        }

        String artist1 = concert1.getArtist().getName();
        String artist2 = concert2.getArtist().getName();
        int artistResult = artist1.compareTo(artist2);

        if (artistResult != 0) {
            return artistResult;
        }

        String location1 = concert1.getLocation().getName();
        String location2 = concert2.getLocation().getName();
        int locationResult = location1.compareTo(location2);

        if (locationResult != 0) {
            return locationResult;
        }

        String id1 = concert1.getId();
        String id2 = concert2.getId();

        return id1.compareTo(id2);
    }

    public static class Builder extends BasePresenterBuilder<Builder, ConcertsPresenter,
            ConcertsUiModel> {
        private CurrentTimeProvider currentTimeProvider;

        public Builder(@NonNull DataSource repository,
                       @NonNull SchedulerProvider schedulerProvider) {
            super(repository, schedulerProvider);
        }

        @NonNull
        @Override
        public ConcertsPresenter build() {
            if (currentTimeProvider == null) {
                throw new IllegalStateException("Current time provider shouldn't be null");
            }

            return new ConcertsPresenter(this);
        }

        @NonNull
        @Override
        public Builder getThis() {
            return this;
        }

        public Builder currentTimeProvider(CurrentTimeProvider currentTimeProvider) {
            this.currentTimeProvider = currentTimeProvider;
            return this;
        }
    }

    interface CurrentTimeProvider {
        Date currentTime();
    }
}
