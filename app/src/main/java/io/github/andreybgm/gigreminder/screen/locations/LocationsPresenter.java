package io.github.andreybgm.gigreminder.screen.locations;

import android.support.annotation.NonNull;

import java.util.Collections;

import io.github.andreybgm.gigreminder.data.Location;
import io.github.andreybgm.gigreminder.repository.DataSource;
import io.github.andreybgm.gigreminder.screen.base.BasePresenter;
import io.github.andreybgm.gigreminder.screen.base.BasePresenterBuilder;
import io.github.andreybgm.gigreminder.screen.base.Result;
import io.github.andreybgm.gigreminder.screen.base.UiEvent;
import io.github.andreybgm.gigreminder.screen.locations.uievent.DeleteLocationErrorConfirmEvent;
import io.github.andreybgm.gigreminder.screen.locations.uievent.DeleteLocationEvent;
import io.github.andreybgm.gigreminder.screen.locations.uievent.DeleteLocationResult;
import io.github.andreybgm.gigreminder.screen.locations.uievent.LoadLocationsErrorConfirmEvent;
import io.github.andreybgm.gigreminder.screen.locations.uievent.LoadLocationsEvent;
import io.github.andreybgm.gigreminder.screen.locations.uievent.LoadLocationsResult;
import io.github.andreybgm.gigreminder.screen.locations.uievent.OpenNewLocationEvent;
import io.github.andreybgm.gigreminder.screen.locations.uievent.SaveLocationErrorConfirmEvent;
import io.github.andreybgm.gigreminder.screen.locations.uievent.SaveLocationEvent;
import io.github.andreybgm.gigreminder.screen.locations.uievent.SaveLocationResult;
import io.github.andreybgm.gigreminder.utils.schedulers.SchedulerProvider;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;

public class LocationsPresenter extends BasePresenter<LocationsUiModel> {

    private final LocationsUiModel initialUiModel;

    public LocationsPresenter(Builder builder) {
        super(builder);

        initialUiModel = makeInitialUiModel(builder);

        connect();
    }

    @Override
    protected LocationsUiModel getInitialUiModel() {
        return initialUiModel;
    }

    @Override
    protected Observable<Result> handleEvents(Observable<UiEvent> events) {
        ObservableTransformer<LoadLocationsEvent, Result> loadLocations = loadEvents ->
                loadEvents.flatMap(event ->
                        getRepository().getLocations()
                                .map(LoadLocationsResult::success)
                                .onErrorReturn(LoadLocationsResult::error)
                                .startWith(LoadLocationsResult.IN_PROGRESS)
                );
        ObservableTransformer<DeleteLocationEvent, Result> deleteLocation = deleteEvents ->
                deleteEvents
                        .flatMap(event -> {
                            int position = event.getPosition();
                            LocationsUiModel model = event.getUiModel();
                            int maxPosition = model.getLocations().size() - 1;

                            if (position > maxPosition) {
                                return Observable.empty();
                            }

                            Location location = model.getLocations().get(position);

                            return Observable.just(location);
                        })
                        .flatMap(location ->
                                getRepository().deleteLocation(location)
                                        .toSingleDefault(DeleteLocationResult.SUCCESS)
                                        .onErrorReturn(DeleteLocationResult::error)
                                        .toObservable()
                                        .startWith(DeleteLocationResult.IN_PROGRESS)
                        );
        ObservableTransformer<SaveLocationEvent, Result> saveLocation = saveEvents ->
                saveEvents.flatMap(event ->
                        getRepository().saveLocation(event.getLocation())
                                .toSingleDefault(SaveLocationResult.SUCCESS)
                                .onErrorReturn(SaveLocationResult::error)
                                .toObservable()
                                .startWith(SaveLocationResult.IN_PROGRESS)
                );

        return events.publish(sharedEvents -> Observable.merge(
                handleEventsOfClass(sharedEvents, LoadLocationsEvent.class, loadLocations),
                handleEventsOfClass(sharedEvents, DeleteLocationEvent.class, deleteLocation),
                handleEventsOfClass(sharedEvents, SaveLocationEvent.class, saveLocation),
                sharedEvents.ofType(Result.class)
        ));
    }

    @Override
    protected LocationsUiModel reduceModel(LocationsUiModel model, Result result) {
        if (result instanceof LoadLocationsResult) {
            return reduceModel(model, (LoadLocationsResult) result);
        } else if (result instanceof LoadLocationsErrorConfirmEvent) {
            return reduceModel(model, (LoadLocationsErrorConfirmEvent) result);
        } else if (result instanceof DeleteLocationResult) {
            return reduceModel(model, (DeleteLocationResult) result);
        } else if (result instanceof DeleteLocationErrorConfirmEvent) {
            return reduceModel(model, (DeleteLocationErrorConfirmEvent) result);
        } else if (result instanceof SaveLocationResult) {
            return reduceModel(model, (SaveLocationResult) result);
        } else if (result instanceof SaveLocationErrorConfirmEvent) {
            return reduceModel(model, (SaveLocationErrorConfirmEvent) result);
        } else if (result instanceof OpenNewLocationEvent) {
            return reduceModel(model, (OpenNewLocationEvent) result);
        }

        throw makeUnknownResultException(result);
    }

    private LocationsUiModel reduceModel(LocationsUiModel model, LoadLocationsResult result) {
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

    private LocationsUiModel reduceModel(LocationsUiModel model,
                                         LoadLocationsErrorConfirmEvent result) {
        return model.copy()
                .loadingError(false)
                .build();
    }

    private LocationsUiModel reduceModel(LocationsUiModel model,
                                         DeleteLocationResult result) {
        if (result.isInProgress()) {
            return model.copy()
                    .deletion(true)
                    .build();
        } else if (result.isSuccess()) {
            return model.copy()
                    .deletion(false)
                    .deletionError(false)
                    .build();
        } else if (result.isError()) {
            return model.copy()
                    .deletion(false)
                    .deletionError(true)
                    .build();
        }

        throw makeUnknownResultException(result);
    }

    private LocationsUiModel reduceModel(LocationsUiModel model,
                                         DeleteLocationErrorConfirmEvent result) {
        return model.copy()
                .deletionError(false)
                .build();
    }

    private LocationsUiModel reduceModel(LocationsUiModel model,
                                         SaveLocationResult result) {
        if (result.isInProgress()) {
            return model.copy()
                    .saving(true)
                    .build();
        } else if (result.isSuccess()) {
            return model.copy()
                    .saving(false)
                    .savingError(false)
                    .build();
        } else if (result.isError()) {
            return model.copy()
                    .saving(false)
                    .savingError(true)
                    .build();
        }

        throw makeUnknownResultException(result);
    }

    private LocationsUiModel reduceModel(LocationsUiModel model,
                                         SaveLocationErrorConfirmEvent result) {
        return model.copy()
                .savingError(false)
                .build();
    }

    private LocationsUiModel reduceModel(LocationsUiModel model,
                                         OpenNewLocationEvent result) {
        boolean shouldOpen = !result.isConfirmation();

        return model.copy()
                .shouldOpenNewLocation(shouldOpen)
                .build();
    }

    private static LocationsUiModel makeInitialUiModel(Builder builder) {
        if (builder.getUiModel().isPresent()) {
            return builder.getUiModel().getValue();
        }

        return LocationsUiModel.DEFAULT;
    }

    public static class Builder extends BasePresenterBuilder<Builder, LocationsPresenter,
            LocationsUiModel> {
        public Builder(@NonNull DataSource repository,
                       @NonNull SchedulerProvider schedulerProvider) {
            super(repository, schedulerProvider);
        }

        @NonNull
        @Override
        public LocationsPresenter build() {
            return new LocationsPresenter(this);
        }

        @NonNull
        @Override
        public Builder getThis() {
            return this;
        }
    }
}
