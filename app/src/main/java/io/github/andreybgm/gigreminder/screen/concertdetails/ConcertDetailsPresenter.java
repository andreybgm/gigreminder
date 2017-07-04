package io.github.andreybgm.gigreminder.screen.concertdetails;

import android.support.annotation.NonNull;

import io.github.andreybgm.gigreminder.data.Concert;
import io.github.andreybgm.gigreminder.repository.DataSource;
import io.github.andreybgm.gigreminder.screen.base.BasePresenter;
import io.github.andreybgm.gigreminder.screen.base.BasePresenterBuilder;
import io.github.andreybgm.gigreminder.screen.base.Result;
import io.github.andreybgm.gigreminder.screen.base.UiEvent;
import io.github.andreybgm.gigreminder.screen.concertdetails.uievent.LoadConcertEvent;
import io.github.andreybgm.gigreminder.screen.concertdetails.uievent.LoadConcertResult;
import io.github.andreybgm.gigreminder.screen.concertdetails.uievent.SiteClickEvent;
import io.github.andreybgm.gigreminder.utils.schedulers.SchedulerProvider;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;

public class ConcertDetailsPresenter extends BasePresenter<ConcertDetailsUiModel> {

    private final ConcertDetailsUiModel initialUiModel;
    private final String concertId;

    public ConcertDetailsPresenter(Builder builder) {
        super(builder);

        initialUiModel = makeInitialUiModel(builder);
        concertId = builder.concertId;

        connect();
    }

    @Override
    protected ConcertDetailsUiModel getInitialUiModel() {
        return initialUiModel;
    }

    @Override
    protected Observable<Result> handleEvents(Observable<UiEvent> events) {
        ObservableTransformer<LoadConcertEvent, Result> loadConcert = loadEvents ->
                loadEvents.flatMap(event -> {
                    return getRepository().getConcert(concertId)
                            .map(LoadConcertResult::success)
                            .onErrorReturn(LoadConcertResult::error)
                            .toObservable()
                            .startWith(LoadConcertResult.IN_PROGRESS);
                });

        return events.publish(sharedEvents -> Observable.merge(
                handleEventsOfClass(sharedEvents, LoadConcertEvent.class, loadConcert),
                sharedEvents.ofType(Result.class)
        ));
    }

    @Override
    protected ConcertDetailsUiModel reduceModel(ConcertDetailsUiModel model, Result result) {
        if (result instanceof LoadConcertResult) {
            LoadConcertResult loadConcertResult = (LoadConcertResult) result;

            if (loadConcertResult.isInProgress()) {
                return model.copy()
                        .loading(true)
                        .build();
            } else if (loadConcertResult.isSuccess()) {
                return model.copy()
                        .loading(false)
                        .concert(loadConcertResult.getConcert().getValue())
                        .build();
            } else if (loadConcertResult.isError()) {
                return model.copy()
                        .loading(false)
                        .loadingError(true)
                        .build();
            }
        } else if (result instanceof SiteClickEvent) {
            SiteClickEvent siteClickEvent = (SiteClickEvent) result;

            if (siteClickEvent.isConfirmation()) {
                return model.copy()
                        .linkToOpen("")
                        .build();
            } else if (model.getConcert().isPresent()) {
                Concert concert = model.getConcert().getValue();

                return model.copy()
                        .linkToOpen(concert.getUrl())
                        .build();
            }
        }

        throw makeUnknownResultException(result);
    }

    private static ConcertDetailsUiModel makeInitialUiModel(Builder builder) {
        if (builder.getUiModel().isPresent()) {
            return builder.getUiModel().getValue();
        }

        return ConcertDetailsUiModel.createDefault(builder.concertId);
    }

    public static class Builder extends BasePresenterBuilder<Builder, ConcertDetailsPresenter,
            ConcertDetailsUiModel> {

        @NonNull
        private String concertId = "";

        public Builder(@NonNull DataSource repository,
                       @NonNull SchedulerProvider schedulerProvider) {
            super(repository, schedulerProvider);
        }

        @NonNull
        @Override
        public ConcertDetailsPresenter build() {
            return new ConcertDetailsPresenter(this);
        }

        @NonNull
        @Override
        public Builder getThis() {
            return this;
        }

        public Builder concertId(@NonNull String concertId) {
            this.concertId = concertId;
            return this;
        }
    }
}
