package io.github.andreybgm.gigreminder.screen.artistimport;

import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.andreybgm.gigreminder.R;
import io.github.andreybgm.gigreminder.data.Artist;
import io.github.andreybgm.gigreminder.repository.DataSource;
import io.github.andreybgm.gigreminder.screen.artistimport.uievent.ArtistClickEvent;
import io.github.andreybgm.gigreminder.screen.artistimport.uievent.LoadArtistsErrorConfirmEvent;
import io.github.andreybgm.gigreminder.screen.artistimport.uievent.LoadArtistsEvent;
import io.github.andreybgm.gigreminder.screen.artistimport.uievent.LoadArtistsResult;
import io.github.andreybgm.gigreminder.screen.artistimport.uievent.SaveArtistsErrorConfirmEvent;
import io.github.andreybgm.gigreminder.screen.artistimport.uievent.SaveArtistsEvent;
import io.github.andreybgm.gigreminder.screen.artistimport.uievent.SaveArtistsResult;
import io.github.andreybgm.gigreminder.screen.artistimport.uievent.SelectAllEvent;
import io.github.andreybgm.gigreminder.screen.artistimport.uievent.UnselectAllEvent;
import io.github.andreybgm.gigreminder.screen.base.BasePresenter;
import io.github.andreybgm.gigreminder.screen.base.BasePresenterBuilder;
import io.github.andreybgm.gigreminder.screen.base.Result;
import io.github.andreybgm.gigreminder.screen.base.UiEvent;
import io.github.andreybgm.gigreminder.utils.Optional;
import io.github.andreybgm.gigreminder.utils.Pair;
import io.github.andreybgm.gigreminder.utils.schedulers.SchedulerProvider;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;

public class ArtistImportPresenter extends BasePresenter<ArtistImportUiModel> {
    private final ArtistImportUiModel initialUiModel;

    public ArtistImportPresenter(Builder builder) {
        super(builder);

        initialUiModel = makeInitialUiModel(builder);

        connect();
    }

    @Override
    protected ArtistImportUiModel getInitialUiModel() {
        return initialUiModel;
    }

    @Override
    protected Observable<Result> handleEvents(Observable<UiEvent> events) {
        ObservableTransformer<LoadArtistsEvent, Result> loadArtists = loadEvents ->
                loadEvents.flatMap(event ->
                        getRepository().loadArtistsFromGoogleMusic()
                                .map(LoadArtistsResult::success)
                                .onErrorReturn(LoadArtistsResult::error)
                                .toObservable()
                                .startWith(LoadArtistsResult.IN_PROGRESS)
                );

        ObservableTransformer<SaveArtistsEvent, Result> saveArtists = saveEvents ->
                saveEvents.flatMap(event -> {
                    List<String> names = event.getUiModel().getSelectedArtists();

                    if (names.isEmpty()) {
                        return Observable.just(SaveArtistsResult.error(
                                R.string.artist_import_error_no_artists_to_save));
                    }

                    List<Artist> artists = Observable.fromIterable(names)
                            .map(Artist::new)
                            .toList()
                            .blockingGet();

                    return getRepository().saveArtists(artists)
                            .toSingleDefault(SaveArtistsResult.SUCCESS)
                            .onErrorReturnItem(SaveArtistsResult.error(R.string.error_saving_data))
                            .toObservable()
                            .startWith(SaveArtistsResult.IN_PROGRESS);
                });

        return events.publish(sharedEvents -> Observable.merge(
                handleEventsOfClass(sharedEvents, LoadArtistsEvent.class, loadArtists),
                handleEventsOfClass(sharedEvents, SaveArtistsEvent.class, saveArtists),
                sharedEvents.ofType(Result.class))
        );
    }

    @Override
    protected ArtistImportUiModel reduceModel(ArtistImportUiModel model, Result result) {
        if (result instanceof LoadArtistsResult) {
            return reduceModel(model, (LoadArtistsResult) result);
        } else if (result instanceof LoadArtistsErrorConfirmEvent) {
            return reduceModel(model, (LoadArtistsErrorConfirmEvent) result);
        } else if (result instanceof ArtistClickEvent) {
            return reduceModel(model, (ArtistClickEvent) result);
        } else if (result instanceof SelectAllEvent) {
            return reduceModelSelectAll(model, true);
        } else if (result instanceof UnselectAllEvent) {
            return reduceModelSelectAll(model, false);
        } else if (result instanceof SaveArtistsResult) {
            return reduceModel(model, (SaveArtistsResult) result);
        } else if (result instanceof SaveArtistsErrorConfirmEvent) {
            return reduceModel(model, (SaveArtistsErrorConfirmEvent) result);
        }

        throw makeUnknownResultException(result);
    }

    private ArtistImportUiModel reduceModel(ArtistImportUiModel model, LoadArtistsResult result) {
        if (result.isInProgress()) {
            return model.copy()
                    .loading(true)
                    .loadingError(false)
                    .build();
        } else if (result.isError()) {
            return model.copy()
                    .loading(false)
                    .loadingError(true)
                    .artists(Collections.emptyList())
                    .build();
        } else if (result.isSuccess()) {
            List<String> names = result.getArtists();

            ArtistImportUiModel.Builder builder = model.copy()
                    .loading(false)
                    .loadingError(false)
                    .firstLoading(false)
                    .artists(names);

            boolean selectAll = model.isFirstLoading()
                    && !model.getInitialSelectedArtistNames().isPresent();
            boolean selectByInitial = !model.isFirstLoading()
                    && model.getInitialSelectedArtistNames().isPresent();

            if (selectByInitial) {
                List<String> initialSelected = model.getInitialSelectedArtistNames().getValue();
                Set<Integer> selected = definePositionsOfSelectedNames(names,
                        new HashSet<>(initialSelected));

                return builder
                        .firstLoading(false)
                        .selectedArtistPositions(Collections.unmodifiableSet(selected))
                        .clearInitialSelectedArtistNames()
                        .build();
            } else if (selectAll) {
                return builder
                        .firstLoading(false)
                        .selectedArtistPositions(selectAllPositions(names))
                        .build();
            }

            return builder
                    .selectedArtistPositions(Collections.emptySet())
                    .build();
        }

        throw makeUnknownResultException(result);
    }

    private ArtistImportUiModel reduceModel(ArtistImportUiModel model,
                                            LoadArtistsErrorConfirmEvent result) {
        return model.copy()
                .loadingError(false)
                .build();
    }

    private ArtistImportUiModel reduceModel(ArtistImportUiModel model,
                                            ArtistClickEvent result) {
        int position = result.getPosition();
        int maxPosition = model.getArtists().size() - 1;

        if (position > maxPosition) {
            return model;
        }

        Set<Integer> currentSelectedPositions = model.getSelectedArtistPositions();
        boolean isCurrentSelected = currentSelectedPositions.contains(position);

        Set<Integer> newSelectedPositions = new HashSet<>(currentSelectedPositions);

        if (isCurrentSelected) {
            newSelectedPositions.remove(position);
        } else {
            newSelectedPositions.add(position);
        }

        return model.copy()
                .selectedArtistPositions(Collections.unmodifiableSet(newSelectedPositions))
                .build();
    }

    private ArtistImportUiModel reduceModelSelectAll(ArtistImportUiModel model, boolean select) {
        if (select) {
            return model.copy()
                    .selectedArtistPositions(selectAllPositions(model.getArtists()))
                    .build();
        } else {
            return model.copy()
                    .selectedArtistPositions(Collections.emptySet())
                    .build();
        }
    }

    private ArtistImportUiModel reduceModel(ArtistImportUiModel model,
                                            SaveArtistsResult result) {
        if (result.isInProgress()) {
            return model.copy()
                    .saving(true)
                    .build();
        } else if (result.isError()) {
            return model.copy()
                    .saving(false)
                    .savingError(Optional.of(result.getError()))
                    .build();
        } else if (result.isSuccess()) {
            return model.copy()
                    .saving(false)
                    .savingError(Optional.empty())
                    .shouldClose(true)
                    .build();
        }

        throw makeUnknownResultException(result);
    }

    private ArtistImportUiModel reduceModel(ArtistImportUiModel model,
                                            SaveArtistsErrorConfirmEvent result) {
        return model.copy()
                .savingError(Optional.empty())
                .build();
    }

    private static ArtistImportUiModel makeInitialUiModel(Builder builder) {
        if (builder.getUiModel().isPresent()) {
            return builder.getUiModel().getValue();
        }

        return ArtistImportUiModel.DEFAULT.copy()
                .firstLoading(builder.firstLoading)
                .initialSelectedArtistNames(builder.selectedArtistNames)
                .build();
    }

    private static Set<Integer> definePositionsOfSelectedNames(List<String> allNames,
                                                               Set<String> selectedNames) {
        return Observable.fromIterable(allNames)
                .zipWith(
                        Observable.range(0, allNames.size()),
                        (name, position) -> Pair.create(position, name)
                )
                .filter(positionAndName -> selectedNames.contains(positionAndName.second))
                .map(positionAndName -> positionAndName.first)
                .reduce(new HashSet<Integer>(), (positions, position) -> {
                    positions.add(position);

                    return positions;
                })
                .blockingGet();
    }

    private static Set<Integer> selectAllPositions(List<String> names) {
        List<Integer> selectedPositions = Observable.range(0, names.size())
                .toList()
                .blockingGet();

        return Collections.unmodifiableSet(
                new HashSet<>(selectedPositions));
    }

    public static class Builder extends BasePresenterBuilder<Builder, ArtistImportPresenter,
            ArtistImportUiModel> {
        @NonNull
        private Optional<List<String>> selectedArtistNames;
        private boolean firstLoading;

        public Builder(DataSource repository, SchedulerProvider schedulerProvider) {
            super(repository, schedulerProvider);
            selectedArtistNames = Optional.empty();
            firstLoading = true;
        }

        @Override
        @NonNull
        public ArtistImportPresenter build() {
            return new ArtistImportPresenter(this);
        }

        @NonNull
        @Override
        public Builder getThis() {
            return this;
        }

        @NonNull
        public Builder selectedArtistNames(@NonNull Optional<List<String>> selectedArtistNames) {
            this.selectedArtistNames = selectedArtistNames;
            return this;
        }

        public Builder firstLoading(boolean firstLoading) {
            this.firstLoading = firstLoading;
            return this;
        }
    }
}
