package io.github.andreybgm.gigreminder.screen.editartist;

import android.support.annotation.NonNull;

import io.github.andreybgm.gigreminder.R;
import io.github.andreybgm.gigreminder.data.Artist;
import io.github.andreybgm.gigreminder.repository.DataSource;
import io.github.andreybgm.gigreminder.repository.error.NotUniqueArtistException;
import io.github.andreybgm.gigreminder.screen.base.BasePresenter;
import io.github.andreybgm.gigreminder.screen.base.BasePresenterBuilder;
import io.github.andreybgm.gigreminder.screen.base.Result;
import io.github.andreybgm.gigreminder.screen.base.UiEvent;
import io.github.andreybgm.gigreminder.screen.editartist.uievent.DiscardConfirmEvent;
import io.github.andreybgm.gigreminder.screen.editartist.uievent.DiscardEvent;
import io.github.andreybgm.gigreminder.screen.editartist.uievent.LoadArtistEvent;
import io.github.andreybgm.gigreminder.screen.editartist.uievent.LoadArtistResult;
import io.github.andreybgm.gigreminder.screen.editartist.uievent.SaveArtistErrorConfirmEvent;
import io.github.andreybgm.gigreminder.screen.editartist.uievent.SaveArtistEvent;
import io.github.andreybgm.gigreminder.screen.editartist.uievent.SaveArtistResult;
import io.github.andreybgm.gigreminder.screen.editartist.uievent.ViewDataIsFilledEvent;
import io.github.andreybgm.gigreminder.utils.Optional;
import io.github.andreybgm.gigreminder.utils.schedulers.SchedulerProvider;
import io.reactivex.Completable;
import io.reactivex.Observable;

public class EditArtistPresenter extends BasePresenter<EditArtistUiModel> {

    private final EditArtistUiModel initialUiModel;
    private final Optional<String> artistId;

    public EditArtistPresenter(Builder builder) {
        super(builder);

        initialUiModel = makeInitialUiModel(builder);
        artistId = builder.artistId;

        connect();
    }

    @Override
    protected EditArtistUiModel getInitialUiModel() {
        return initialUiModel;
    }

    @Override
    protected Observable<Result> handleEvents(Observable<UiEvent> events) {
        return events.publish(sharedEvents -> Observable.merge(
                handleEventsOfClass(sharedEvents, LoadArtistEvent.class, this::loadArtist),
                handleEventsOfClass(sharedEvents, SaveArtistEvent.class, this::saveArtist),
                sharedEvents.ofType(Result.class)
        ));
    }

    @Override
    protected EditArtistUiModel reduceModel(EditArtistUiModel model, Result result) {
        if (result instanceof LoadArtistResult) {
            return reduceModel(model, (LoadArtistResult) result);
        } else if (result instanceof ViewDataIsFilledEvent) {
            return reduceModel(model, (ViewDataIsFilledEvent) result);
        } else if (result instanceof SaveArtistResult) {
            return reduceModel(model, (SaveArtistResult) result);
        } else if (result instanceof SaveArtistErrorConfirmEvent) {
            return reduceModel(model, (SaveArtistErrorConfirmEvent) result);
        } else if (result instanceof DiscardEvent) {
            return reduceModel(model, (DiscardEvent) result);
        } else if (result instanceof DiscardConfirmEvent) {
            return reduceModel(model, (DiscardConfirmEvent) result);
        }

        throw makeUnknownResultException(result);
    }

    private EditArtistUiModel reduceModel(EditArtistUiModel model, LoadArtistResult result) {
        if (result.isInProgress()) {
            return model.copy()
                    .loading(true)
                    .build();
        } else if (result.isSuccess()) {
            return model.copy()
                    .loading(false)
                    .initialArtist(result.getArtist())
                    .build();
        } else if (result.isError()) {
            return model.copy()
                    .loading(false)
                    .loadingError(true)
                    .build();
        }

        throw makeUnknownResultException(result);
    }

    private EditArtistUiModel reduceModel(EditArtistUiModel model,
                                          ViewDataIsFilledEvent result) {
        return model.copy()
                .viewDataIsFilled(true)
                .build();
    }

    private EditArtistUiModel reduceModel(EditArtistUiModel model, SaveArtistResult result) {
        if (result.isFillError()) {
            EditArtistUiModel.Builder builder = model.copy()
                    .clearSavingData()
                    .fillError(true);

            if (result.isNameEmpty()) {
                builder = builder.emptyNameError(true);
            }

            if (result.isNameNotUnique()) {
                builder = builder.notUniqueNameError(true);
            }

            return builder.build();
        } else if (result.isSavingError()) {
            return model.copy()
                    .clearSavingData()
                    .savingError(true)
                    .build();
        } else if (result.isInProgress()) {
            return model.copy()
                    .clearSavingData()
                    .saving(true)
                    .build();
        } else if (result.isSuccess()) {
            return model.copy()
                    .clearSavingData()
                    .shouldClose(true)
                    .build();
        }

        throw makeUnknownResultException(result);
    }

    private EditArtistUiModel reduceModel(EditArtistUiModel model,
                                          SaveArtistErrorConfirmEvent result) {
        return model.copy()
                .savingError(false)
                .build();
    }

    private EditArtistUiModel reduceModel(EditArtistUiModel model, DiscardEvent result) {
        if (result.isForceClose()) {
            return model.copy()
                    .clearDiscardData()
                    .shouldClose(true)
                    .build();
        } else if (model.isArtistNew()) {
            if (!result.getName().isEmpty()) {
                return model.copy()
                        .clearDiscardData()
                        .shouldAskToDiscard(true)
                        .discardMsg(Optional.of(R.string.dialog_discard_new_artist))
                        .build();
            }

            return model.copy()
                    .clearDiscardData()
                    .shouldClose(true)
                    .build();
        } else if (!model.isArtistNew()) {
            String newName = result.getName();
            String initialName = model.getInitialArtist().getValue().getName();
            boolean nameIsChanged = !newName.equals(initialName);

            if (nameIsChanged) {
                return model.copy()
                        .clearDiscardData()
                        .shouldAskToDiscard(true)
                        .discardMsg(Optional.of(R.string.dialog_discard_artist_changes))
                        .build();
            }

            return model.copy()
                    .clearDiscardData()
                    .shouldClose(true)
                    .build();
        }

        throw makeUnknownResultException(result);
    }

    private EditArtistUiModel reduceModel(EditArtistUiModel model, DiscardConfirmEvent result) {
        return model.copy()
                .clearDiscardData()
                .build();
    }

    private static EditArtistUiModel makeInitialUiModel(Builder builder) {
        if (builder.getUiModel().isPresent()) {
            return builder.getUiModel().getValue();
        } else if (builder.artistId.isPresent()) {
            return EditArtistUiModel.Builder.create()
                    .artistId(builder.artistId)
                    .viewDataIsFilled(builder.viewDataIsFilled)
                    .build();
        }

        return EditArtistUiModel.NEW_ARTIST;
    }

    private Observable<Result> loadArtist(Observable<LoadArtistEvent> events) {
        return events.flatMap(event -> {
            if (!artistId.isPresent()) {
                throw new RuntimeException("An artist id shouldn't be empty");
            }

            return getRepository().getArtist(artistId.getValue())
                    .map(LoadArtistResult::success)
                    .onErrorReturn(LoadArtistResult::error)
                    .toObservable()
                    .startWith(LoadArtistResult.IN_PROGRESS);
        });
    }

    private Observable<Result> saveArtist(Observable<SaveArtistEvent> events) {
        return events.flatMap(event -> {
            String newName = event.getName();

            if (newName.isEmpty()) {
                SaveArtistResult result = SaveArtistResult.Builder.createFillError()
                        .nameIsEmpty(true)
                        .build();

                return Observable.just(result);
            }

            EditArtistUiModel uiModel = event.getUiModel();
            Completable completable;

            if (uiModel.isArtistNew()) {
                Artist newArtist = new Artist(newName);
                completable = getRepository().saveArtist(newArtist);
            } else {
                Artist initialArtist = uiModel.getInitialArtist().getValue();
                Artist updatedArtist = new Artist(initialArtist.getId(), newName);
                completable = getRepository().updateArtist(updatedArtist);
            }

            return completable
                    .toSingleDefault(SaveArtistResult.SUCCESS)
                    .onErrorReturn(t -> {
                        if (t instanceof NotUniqueArtistException) {
                            return SaveArtistResult.Builder.createFillError()
                                    .nameIsNotUnique(true)
                                    .build();
                        }

                        return SaveArtistResult.SAVING_ERROR;
                    })
                    .toObservable()
                    .startWith(SaveArtistResult.IN_PROGRESS);
        });
    }

    public static class Builder extends BasePresenterBuilder<Builder, EditArtistPresenter,
            EditArtistUiModel> {

        @NonNull
        private Optional<String> artistId;
        private boolean viewDataIsFilled;

        public Builder(@NonNull DataSource repository,
                       @NonNull SchedulerProvider schedulerProvider) {
            super(repository, schedulerProvider);

            artistId = Optional.empty();
        }

        @NonNull
        @Override
        public EditArtistPresenter build() {
            return new EditArtistPresenter(this);
        }

        @NonNull
        @Override
        public Builder getThis() {
            return this;
        }

        public Builder artistId(@NonNull String artistId) {
            this.artistId = Optional.of(artistId);
            return this;
        }

        public Builder viewDataIsFilled(boolean viewDataIsFilled) {
            this.viewDataIsFilled = viewDataIsFilled;
            return this;
        }
    }
}
