package io.github.andreybgm.gigreminder.screen.artists;

import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.andreybgm.gigreminder.data.Artist;
import io.github.andreybgm.gigreminder.repository.DataSource;
import io.github.andreybgm.gigreminder.screen.artists.uievent.ArtistClickEvent;
import io.github.andreybgm.gigreminder.screen.artists.uievent.ArtistLongClickEvent;
import io.github.andreybgm.gigreminder.screen.artists.uievent.DeleteArtistErrorConfirmEvent;
import io.github.andreybgm.gigreminder.screen.artists.uievent.DeleteArtistsEvent;
import io.github.andreybgm.gigreminder.screen.artists.uievent.DeleteArtistsResult;
import io.github.andreybgm.gigreminder.screen.artists.uievent.LoadArtistsErrorConfirmEvent;
import io.github.andreybgm.gigreminder.screen.artists.uievent.LoadArtistsEvent;
import io.github.andreybgm.gigreminder.screen.artists.uievent.LoadArtistsResult;
import io.github.andreybgm.gigreminder.screen.artists.uievent.OpenArtistConfirmEvent;
import io.github.andreybgm.gigreminder.screen.artists.uievent.OpenArtistsImportEvent;
import io.github.andreybgm.gigreminder.screen.artists.uievent.OpenNewArtistEvent;
import io.github.andreybgm.gigreminder.screen.artists.uievent.TurnOffActionModeEvent;
import io.github.andreybgm.gigreminder.screen.base.BasePresenter;
import io.github.andreybgm.gigreminder.screen.base.BasePresenterBuilder;
import io.github.andreybgm.gigreminder.screen.base.Result;
import io.github.andreybgm.gigreminder.screen.base.UiEvent;
import io.github.andreybgm.gigreminder.utils.Pair;
import io.github.andreybgm.gigreminder.utils.schedulers.SchedulerProvider;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;

public class ArtistsPresenter extends BasePresenter<ArtistsUiModel> {
    private final ArtistsUiModel initialUiModel;

    public ArtistsPresenter(Builder builder) {
        super(builder);

        initialUiModel = makeInitialUiModel(builder);

        connect();
    }

    @Override
    protected ArtistsUiModel getInitialUiModel() {
        return initialUiModel;
    }

    @Override
    protected Observable<Result> handleEvents(Observable<UiEvent> events) {
        ObservableTransformer<LoadArtistsEvent, Result> loadArtists = loadEvents ->
                loadEvents.flatMap(event ->
                        getRepository().getArtists()
                                .map(LoadArtistsResult::success)
                                .onErrorReturn(LoadArtistsResult::error)
                                .startWith(LoadArtistsResult.IN_PROGRESS)
                );
        ObservableTransformer<DeleteArtistsEvent, Result> deleteArtists = deleteEvents ->
                deleteEvents.flatMap(event -> {
                    List<Artist> artists = event.getUiModel().getSelectedArtists();

                    return getRepository().deleteArtists(artists)
                            .toSingleDefault(DeleteArtistsResult.SUCCESS)
                            .onErrorReturn(DeleteArtistsResult::error)
                            .toObservable()
                            .startWith(DeleteArtistsResult.IN_PROGRESS);
                });

        return events.publish(sharedEvents -> Observable.merge(
                handleEventsOfClass(sharedEvents, LoadArtistsEvent.class, loadArtists),
                handleEventsOfClass(sharedEvents, DeleteArtistsEvent.class,
                        deleteArtists),
                sharedEvents.ofType(Result.class))
        );
    }

    @Override
    protected ArtistsUiModel reduceModel(ArtistsUiModel model, Result result) {
        if (result instanceof LoadArtistsResult) {
            return reduceModel(model, (LoadArtistsResult) result);
        } else if (result instanceof LoadArtistsErrorConfirmEvent) {
            return reduceModel(model, (LoadArtistsErrorConfirmEvent) result);
        } else if (result instanceof ArtistClickEvent) {
            return reduceModel(model, (ArtistClickEvent) result);
        } else if (result instanceof OpenArtistConfirmEvent) {
            return reduceModel(model, (OpenArtistConfirmEvent) result);
        } else if (result instanceof ArtistLongClickEvent) {
            return reduceModel(model, (ArtistLongClickEvent) result);
        } else if (result instanceof TurnOffActionModeEvent) {
            return reduceModel(model, (TurnOffActionModeEvent) result);
        } else if (result instanceof DeleteArtistsResult) {
            return reduceModel(model, (DeleteArtistsResult) result);
        } else if (result instanceof DeleteArtistErrorConfirmEvent) {
            return reduceModel(model, (DeleteArtistErrorConfirmEvent) result);
        } else if (result instanceof OpenArtistsImportEvent) {
            return reduceModel(model, (OpenArtistsImportEvent) result);
        } else if (result instanceof OpenNewArtistEvent) {
            return reduceModel(model, (OpenNewArtistEvent) result);
        }

        throw makeUnknownResultException(result);
    }

    private static ArtistsUiModel makeInitialUiModel(Builder builder) {
        if (builder.getUiModel().isPresent()) {
            return builder.getUiModel().getValue();
        }

        return ArtistsUiModel.DEFAULT.copy()
                .actionModeIsExpected(builder.actionModeOn)
                .initialSelectedArtistIds(builder.selectedArtistIds)
                .build();
    }

    private ArtistsUiModel reduceModel(ArtistsUiModel model, LoadArtistsResult result) {
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
            List<Artist> artists = result.getArtists();

            ArtistsUiModel.Builder builder = model.copy()
                    .loading(false)
                    .loadingError(false)
                    .artists(artists);

            if (model.isActionModeExpected()) {
                Set<String> selectedIds = new HashSet<>(model.getInitialSelectedArtistIds());
                Set<Integer> selectedPositions = defineSelectedPositions(artists, selectedIds);

                if (!selectedPositions.isEmpty()) {
                    return builder
                            .actionMode(true)
                            .selectedArtistPositions(Collections.unmodifiableSet(selectedPositions))
                            .actionModeIsExpected(false)
                            .initialSelectedArtistIds(Collections.emptyList())
                            .build();
                }
            } else if (model.isActionModeOn()) {
                Set<String> selectedIds = new HashSet<>(model.getSelectedArtistIds());
                Set<Integer> selectedPositions = defineSelectedPositions(artists, selectedIds);

                if (selectedPositions.isEmpty()) {
                    return builder
                            .turnOffActionMode()
                            .build();
                }

                return builder
                        .selectedArtistPositions(selectedPositions)
                        .build();
            }

            return builder.build();
        }

        throw makeUnknownResultException(result);
    }

    private static Set<Integer> defineSelectedPositions(List<Artist> artists,
                                                        Set<String> selectedIds) {
        return Observable.fromIterable(artists)
                .scan(Pair.<Integer, Artist>create(-1, null),
                        (previous, artist) -> Pair.create(previous.first + 1, artist)
                )
                .filter(positionAndArtist -> positionAndArtist.first != -1
                        && selectedIds.contains(positionAndArtist.second.getId())
                )
                .map(positionAndArtist -> positionAndArtist.first)
                .reduce(new HashSet<Integer>(), (positions, position) -> {
                    positions.add(position);
                    return positions;
                })
                .blockingGet();
    }

    private ArtistsUiModel reduceModel(ArtistsUiModel model,
                                       LoadArtistsErrorConfirmEvent result) {
        return model.copy()
                .loadingError(false)
                .build();
    }

    private ArtistsUiModel reduceModel(ArtistsUiModel model, ArtistClickEvent result) {
        int position = result.getPosition();
        int maxPosition = model.getArtists().size() - 1;

        if (position > maxPosition) {
            return model;
        }

        if (model.isActionModeOn()) {
            Set<Integer> currentSelectedPositions = model.getSelectedArtistPositions();
            boolean isCurrentSelected = currentSelectedPositions.contains(position);
            boolean currentPositionIsOnlySelected = currentSelectedPositions.size() == 1
                    && isCurrentSelected;

            if (currentPositionIsOnlySelected) {
                return model.copy()
                        .turnOffActionMode()
                        .build();
            }

            Set<Integer> newSelectedPositions = new HashSet<>(currentSelectedPositions);

            if (isCurrentSelected) {
                newSelectedPositions.remove(position);
            } else {
                newSelectedPositions.add(position);
            }

            return model.copy()
                    .selectedArtistPositions(Collections.unmodifiableSet(newSelectedPositions))
                    .build();
        } else {
            Artist artist = model.getArtists().get(position);

            return model.copy()
                    .openArtist(artist)
                    .build();
        }
    }

    private ArtistsUiModel reduceModel(ArtistsUiModel model, OpenArtistConfirmEvent result) {
        return model.copy()
                .clearArtistToOpen()
                .build();
    }

    private ArtistsUiModel reduceModel(ArtistsUiModel model, ArtistLongClickEvent result) {
        if (model.isActionModeOn()) {
            return model;
        }

        int position = result.getPosition();
        int maxPosition = model.getArtists().size() - 1;

        if (position > maxPosition) {
            return model;
        }

        Set<Integer> selectedPositions = new HashSet<>();
        selectedPositions.add(position);

        return model.copy()
                .actionMode(true)
                .selectedArtistPositions(Collections.unmodifiableSet(selectedPositions))
                .build();
    }

    private ArtistsUiModel reduceModel(ArtistsUiModel model, TurnOffActionModeEvent result) {
        return model.copy()
                .turnOffActionMode()
                .build();
    }

    private ArtistsUiModel reduceModel(ArtistsUiModel model, DeleteArtistsResult result) {
        if (result.isInProgress()) {
            return model.copy()
                    .deletion(true)
                    .build();
        } else if (result.isSuccess()) {
            return model.copy()
                    .deletion(false)
                    .turnOffActionMode()
                    .build();
        } else if (result.isError()) {
            return model.copy()
                    .deletion(false)
                    .deletionError(true)
                    .build();
        }

        throw makeUnknownResultException(result);
    }

    private ArtistsUiModel reduceModel(ArtistsUiModel model,
                                       DeleteArtistErrorConfirmEvent result) {
        return model.copy()
                .deletion(false)
                .deletionError(false)
                .build();
    }

    private ArtistsUiModel reduceModel(ArtistsUiModel model, OpenArtistsImportEvent result) {
        return model.copy()
                .shouldOpenArtistsImport(!result.isConfirmation())
                .build();
    }

    private ArtistsUiModel reduceModel(ArtistsUiModel model, OpenNewArtistEvent result) {
        return model.copy()
                .shouldOpenNewArtist(!result.isConfirmation())
                .build();
    }


    public static class Builder extends BasePresenterBuilder<Builder, ArtistsPresenter,
            ArtistsUiModel> {

        private boolean actionModeOn;
        @NonNull
        private List<String> selectedArtistIds;

        public Builder(DataSource repository, SchedulerProvider schedulerProvider) {
            super(repository, schedulerProvider);
            selectedArtistIds = Collections.emptyList();
        }

        @Override
        @NonNull
        public ArtistsPresenter build() {
            return new ArtistsPresenter(this);
        }

        @NonNull
        @Override
        public Builder getThis() {
            return this;
        }

        public Builder actionMode(boolean actionModeOn, @NonNull List<String> artistIds) {
            this.actionModeOn = actionModeOn;
            this.selectedArtistIds = artistIds;

            return this;
        }
    }
}
