package io.github.andreybgm.gigreminder.screen.editartist;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import io.github.andreybgm.gigreminder.R;
import io.github.andreybgm.gigreminder.data.Artist;
import io.github.andreybgm.gigreminder.repository.DataSource;
import io.github.andreybgm.gigreminder.repository.error.NotUniqueArtistException;
import io.github.andreybgm.gigreminder.screen.editartist.uievent.DiscardConfirmEvent;
import io.github.andreybgm.gigreminder.screen.editartist.uievent.DiscardEvent;
import io.github.andreybgm.gigreminder.screen.editartist.uievent.LoadArtistEvent;
import io.github.andreybgm.gigreminder.screen.editartist.uievent.SaveArtistErrorConfirmEvent;
import io.github.andreybgm.gigreminder.screen.editartist.uievent.SaveArtistEvent;
import io.github.andreybgm.gigreminder.screen.editartist.uievent.ViewDataIsFilledEvent;
import io.github.andreybgm.gigreminder.utils.schedulers.ImmediateSchedulerProvider;
import io.github.andreybgm.gigreminder.utils.schedulers.SchedulerProvider;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EditArtistPresenterTest {

    private static final String ARTIST_NAME = "Artist1";
    private static final String NEW_ARTIST_NAME = "New Artist1";
    private static final String EMPTY_ARTIST_NAME = "";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private DataSource repository;

    private EditArtistPresenter newArtistPresenter;
    private EditArtistPresenter existingArtistPresenter;
    private Artist artist;
    private EditArtistPresenter.Builder presenterBuilder;
    private SchedulerProvider schedulerProvider = new ImmediateSchedulerProvider();

    @Before
    public void setUp() throws Exception {
        artist = new Artist("AR-1", ARTIST_NAME);

        newArtistPresenter = new EditArtistPresenter.Builder(repository, schedulerProvider)
                .build();

        existingArtistPresenter = new EditArtistPresenter.Builder(repository, schedulerProvider)
                .artistId(artist.getId())
                .build();

        presenterBuilder = new EditArtistPresenter.Builder(repository, schedulerProvider);
    }

    @Test
    public void createNewArtistPresenter() throws Exception {
        newArtistPresenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    assertThat(model.isArtistNew()).isTrue();

                    return true;
                });
    }

    @Test
    public void loadArtist() throws Exception {
        when(repository.getArtist(artist.getId())).thenReturn(Single.just(artist));
        TestObserver<EditArtistUiModel> observer = existingArtistPresenter.getUiModels()
                .test();

        // load
        existingArtistPresenter.sendUiEvent(LoadArtistEvent.INSTANCE);

        observer
                .awaitCount(3)
                .assertNoErrors()
                .assertValueAt(0, model -> {
                    assertThat(model.isArtistNew()).isFalse();
                    assertThat(model.isViewDataIsFilled()).isFalse();

                    return true;
                })
                .assertValueAt(1, model -> {
                    assertThat(model.isLoading()).isTrue();

                    return true;
                })
                .assertValueAt(2, model -> {
                    assertThat(model.isLoading()).isFalse();
                    assertThat(model.isLoadingError()).isFalse();
                    assertThat(model.isViewDataIsFilled()).isFalse();
                    assertThat(model.getInitialArtist().isPresent()).isTrue();
                    assertThat(model.getInitialArtist().getValue()).isEqualTo(artist);

                    return true;
                });

        // confirm
        existingArtistPresenter.sendUiEvent(ViewDataIsFilledEvent.INSTANCE);

        existingArtistPresenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    assertThat(model.isViewDataIsFilled()).isTrue();

                    return true;
                });
    }

    @Test
    public void loadArtistWhenError() throws Exception {
        when(repository.getArtist(artist.getId())).thenReturn(Single.error(new RuntimeException()));

        existingArtistPresenter.sendUiEvent(LoadArtistEvent.INSTANCE);

        existingArtistPresenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    assertThat(model.isLoading()).isFalse();
                    assertThat(model.isLoadingError()).isTrue();

                    return true;
                });
    }

    @Test
    public void createPresenterWhenViewDataIsFilled() throws Exception {
        EditArtistPresenter presenter = presenterBuilder
                .artistId(artist.getId())
                .viewDataIsFilled(true)
                .build();

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    assertThat(model.isArtistNew()).isFalse();
                    assertThat(model.isViewDataIsFilled()).isTrue();

                    return true;
                });
    }

    @Test
    public void saveNewArtist() throws Exception {
        when(repository.saveArtist(any())).thenReturn(Completable.complete());
        EditArtistUiModel uiModel = newArtistPresenter.getInitialUiModel();
        TestObserver<EditArtistUiModel> observer = newArtistPresenter.getUiModels()
                .skip(1)
                .test();

        newArtistPresenter.sendUiEvent(SaveArtistEvent.create(uiModel, NEW_ARTIST_NAME));

        verify(repository).saveArtist(argThat(savingArtist -> {
            assertThat(savingArtist.getName()).isEqualTo(NEW_ARTIST_NAME);

            return true;
        }));
        observer
                .awaitCount(2)
                .assertNoErrors()
                .assertValueAt(0, model -> {
                    assertThat(model.isSaving()).isTrue();

                    return true;
                })
                .assertValueAt(1, model -> {
                    assertThat(model.isSaving()).isFalse();
                    assertThat(model.isSavingError()).isFalse();
                    assertThat(model.isFillError()).isFalse();
                    assertThat(model.isShouldClose()).isTrue();

                    return true;
                });
    }

    @Test
    public void saveExistingArtist() throws Exception {
        when(repository.updateArtist(any())).thenReturn(Completable.complete());
        EditArtistUiModel uiModel = createLoadedArtistUiModel();
        EditArtistPresenter presenter = presenterBuilder
                .uiModel(uiModel)
                .build();
        TestObserver<EditArtistUiModel> observer = presenter.getUiModels()
                .skip(1)
                .test();

        presenter.sendUiEvent(SaveArtistEvent.create(
                uiModel, NEW_ARTIST_NAME));

        verify(repository).updateArtist(argThat(savingArtist -> {
            assertThat(savingArtist.getId()).isEqualTo(artist.getId());
            assertThat(savingArtist.getName()).isEqualTo(NEW_ARTIST_NAME);

            return true;
        }));
        observer
                .awaitCount(2)
                .assertNoErrors()
                .assertValueAt(0, model -> {
                    assertThat(model.isSaving()).isTrue();

                    return true;
                })
                .assertValueAt(1, model -> {
                    assertThat(model.isSaving()).isFalse();
                    assertThat(model.isSavingError()).isFalse();
                    assertThat(model.isFillError()).isFalse();
                    assertThat(model.isShouldClose()).isTrue();

                    return true;
                });
    }

    @Test
    public void saveArtistWhenNameIsEmpty() throws Exception {
        EditArtistPresenter presenter = presenterBuilder
                .uiModel(EditArtistUiModel.NEW_ARTIST)
                .build();

        presenter.sendUiEvent(SaveArtistEvent.create(
                EditArtistUiModel.NEW_ARTIST, EMPTY_ARTIST_NAME));

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    assertThat(model.isSaving()).isFalse();
                    assertThat(model.isSavingError()).isFalse();
                    assertThat(model.isFillError()).isTrue();
                    assertThat(model.isEmptyNameError()).isTrue();
                    assertThat(model.isNotUniqueNameError()).isFalse();

                    return true;
                });
    }

    @Test
    public void saveArtistWhenNameIsNotUnique() throws Exception {
        when(repository.saveArtist(any())).thenReturn(Completable.error(
                new NotUniqueArtistException()));
        EditArtistPresenter presenter = presenterBuilder
                .uiModel(EditArtistUiModel.NEW_ARTIST)
                .build();

        presenter.sendUiEvent(SaveArtistEvent.create(
                EditArtistUiModel.NEW_ARTIST, NEW_ARTIST_NAME));

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    assertThat(model.isSaving()).isFalse();
                    assertThat(model.isSavingError()).isFalse();
                    assertThat(model.isFillError()).isTrue();
                    assertThat(model.isEmptyNameError()).isFalse();
                    assertThat(model.isNotUniqueNameError()).isTrue();

                    return true;
                });
    }

    @Test
    public void saveArtistWhenUnknownError() throws Exception {
        when(repository.saveArtist(any())).thenReturn(Completable.error(
                new RuntimeException()));
        EditArtistPresenter presenter = presenterBuilder
                .uiModel(EditArtistUiModel.NEW_ARTIST)
                .build();

        // show error
        presenter.sendUiEvent(SaveArtistEvent.create(
                EditArtistUiModel.NEW_ARTIST, NEW_ARTIST_NAME));

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    assertThat(model.isSaving()).isFalse();
                    assertThat(model.isSavingError()).isTrue();
                    assertThat(model.isFillError()).isFalse();

                    return true;
                });

        // confirm
        presenter.sendUiEvent(SaveArtistErrorConfirmEvent.INSTANCE);

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    assertThat(model.isSavingError()).isFalse();

                    return true;
                });
    }

    @Test
    public void discardNewArtistWhenNameIsEmpty() throws Exception {
        newArtistPresenter.sendUiEvent(DiscardEvent.create(EMPTY_ARTIST_NAME, false));

        newArtistPresenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    assertThatModelShouldClose(model);

                    return true;
                });
    }

    @Test
    public void discardNewArtistWhenNameIsFilled() throws Exception {
        // show discard
        newArtistPresenter.sendUiEvent(DiscardEvent.create(NEW_ARTIST_NAME, false));

        newArtistPresenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    assertThatModelShouldAskToDiscard(model, R.string.dialog_discard_new_artist);

                    return true;
                });

        // confirm
        newArtistPresenter.sendUiEvent(DiscardConfirmEvent.INSTANCE);

        newArtistPresenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    assertThat(model.isShouldAskToDiscard()).isFalse();
                    assertThat(model.getDiscardMsg().isPresent()).isFalse();

                    return true;
                });
    }

    @Test
    public void forceDiscardChanges() throws Exception {
        newArtistPresenter.sendUiEvent(DiscardEvent.create(NEW_ARTIST_NAME, true));

        newArtistPresenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    assertThatModelShouldClose(model);

                    return true;
                });
    }

    @Test
    public void discardExistingArtistWhenItIsNotChanged() throws Exception {
        EditArtistUiModel uiModel = createLoadedArtistUiModel();
        EditArtistPresenter presenter = presenterBuilder
                .uiModel(uiModel)
                .build();

        presenter.sendUiEvent(DiscardEvent.create(ARTIST_NAME, false));

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    assertThatModelShouldClose(model);

                    return true;
                });
    }

    @Test
    public void discardExistingArtistWhenItIsChanged() throws Exception {
        EditArtistUiModel uiModel = createLoadedArtistUiModel();
        EditArtistPresenter presenter = presenterBuilder
                .uiModel(uiModel)
                .build();

        presenter.sendUiEvent(DiscardEvent.create(NEW_ARTIST_NAME, false));

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    assertThatModelShouldAskToDiscard(model,
                            R.string.dialog_discard_artist_changes);

                    return true;
                });
    }

    private void assertThatModelShouldClose(EditArtistUiModel model) {
        assertThat(model.isShouldClose()).isTrue();
        assertThat(model.isShouldAskToDiscard()).isFalse();
    }

    private void assertThatModelShouldAskToDiscard(EditArtistUiModel model, int discardMsg) {
        assertThat(model.isShouldClose()).isFalse();
        assertThat(model.isShouldAskToDiscard()).isTrue();
        assertThat(model.getDiscardMsg().isPresent()).isTrue();
        assertThat(model.getDiscardMsg().getValue()).isEqualTo(discardMsg);
    }

    private EditArtistUiModel createLoadedArtistUiModel() {
        when(repository.getArtist(artist.getId())).thenReturn(Single.just(artist));
        EditArtistPresenter presenter = new EditArtistPresenter.Builder(
                repository, schedulerProvider)
                .artistId(artist.getId())
                .build();

        presenter.sendUiEvent(LoadArtistEvent.INSTANCE);

        return presenter.getUiModels().firstOrError().blockingGet();
    }
}