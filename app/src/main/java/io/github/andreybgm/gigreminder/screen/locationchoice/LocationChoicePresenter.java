package io.github.andreybgm.gigreminder.screen.locationchoice;

import android.support.annotation.NonNull;

import java.util.Collections;

import io.github.andreybgm.gigreminder.data.Location;
import io.github.andreybgm.gigreminder.repository.DataSource;
import io.github.andreybgm.gigreminder.screen.base.BasePresenter;
import io.github.andreybgm.gigreminder.screen.base.BasePresenterBuilder;
import io.github.andreybgm.gigreminder.screen.base.Result;
import io.github.andreybgm.gigreminder.screen.base.UiEvent;
import io.github.andreybgm.gigreminder.screen.locationchoice.uievent.LoadLocationsErrorConfirmEvent;
import io.github.andreybgm.gigreminder.screen.locationchoice.uievent.LoadLocationsEvent;
import io.github.andreybgm.gigreminder.screen.locationchoice.uievent.LoadLocationsResult;
import io.github.andreybgm.gigreminder.screen.locationchoice.uievent.LocationClickEvent;
import io.github.andreybgm.gigreminder.utils.schedulers.SchedulerProvider;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;

public class LocationChoicePresenter extends BasePresenter<LocationChoiceUiModel> {

    private final LocationChoiceUiModel initialUiModel;

    public LocationChoicePresenter(Builder builder) {
        super(builder);

        initialUiModel = makeInitialUiModel(builder);

        connect();
    }

    @Override
    protected LocationChoiceUiModel getInitialUiModel() {
        return initialUiModel;
    }

    @Override
    protected Observable<Result> handleEvents(Observable<UiEvent> events) {
        ObservableTransformer<LoadLocationsEvent, Result> loadLocations = loadEvents ->
                loadEvents.flatMap(event ->
                        getRepository().getAvailableLocations()
                                .map(LoadLocationsResult::success)
                                .onErrorReturn(LoadLocationsResult::error)
                                .toObservable()
                                .startWith(LoadLocationsResult.IN_PROGRESS)
                );
        return events.publish(sharedEvents -> Observable.merge(
                handleEventsOfClass(sharedEvents, LoadLocationsEvent.class, loadLocations),
                sharedEvents.ofType(Result.class)
        ));
    }

    @Override
    protected LocationChoiceUiModel reduceModel(LocationChoiceUiModel model, Result result) {
        if (result instanceof LoadLocationsResult) {
            return reduceModel(model, (LoadLocationsResult) result);
        } else if (result instanceof LoadLocationsErrorConfirmEvent) {
            return reduceModel(model, (LoadLocationsErrorConfirmEvent) result);
        } else if (result instanceof LocationClickEvent) {
            return reduceModel(model, (LocationClickEvent) result);
        }

        throw makeUnknownResultException(result);
    }

    private LocationChoiceUiModel reduceModel(LocationChoiceUiModel model,
                                              LoadLocationsResult result) {
        if (result.isInProgress()) {
            return model.copy()
                    .loading(true)
                    .build();
        } else if (result.isSuccess()) {
            return model.copy()
                    .loading(false)
                    .loadingError(false)
                    .locations(result.getLocations())
                    .build();
        } else if (result.isError()) {
            return model.copy()
                    .loading(false)
                    .loadingError(true)
                    .locations(Collections.emptyList())
                    .build();
        }

        throw makeUnknownResultException(result);
    }

    private LocationChoiceUiModel reduceModel(LocationChoiceUiModel model,
                                              LoadLocationsErrorConfirmEvent result) {
        return model.copy()
                .loadingError(false)
                .build();
    }

    private LocationChoiceUiModel reduceModel(LocationChoiceUiModel model,
                                              LocationClickEvent result) {
        int position = result.getPosition();
        int maxPosition = model.getLocations().size() - 1;

        if (position > maxPosition) {
            return model;
        }

        Location location = model.getLocations().get(position);

        return model.copy()
                .locationToOpen(location)
                .build();
    }

    private static LocationChoiceUiModel makeInitialUiModel(Builder builder) {
        if (builder.getUiModel().isPresent()) {
            return builder.getUiModel().getValue();
        }

        return LocationChoiceUiModel.DEFAULT;
    }

    public static class Builder extends BasePresenterBuilder<Builder, LocationChoicePresenter,
            LocationChoiceUiModel> {
        public Builder(@NonNull DataSource repository,
                       @NonNull SchedulerProvider schedulerProvider) {
            super(repository, schedulerProvider);
        }

        @NonNull
        @Override
        public LocationChoicePresenter build() {
            return new LocationChoicePresenter(this);
        }

        @NonNull
        @Override
        public Builder getThis() {
            return this;
        }
    }
}
