package io.github.andreybgm.gigreminder.screen.artists;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import io.github.andreybgm.gigreminder.data.Artist;
import io.github.andreybgm.gigreminder.repository.DataSource;
import io.github.andreybgm.gigreminder.screen.artists.uievent.ArtistClickEvent;
import io.github.andreybgm.gigreminder.screen.artists.uievent.ArtistLongClickEvent;
import io.github.andreybgm.gigreminder.screen.artists.uievent.DeleteArtistErrorConfirmEvent;
import io.github.andreybgm.gigreminder.screen.artists.uievent.DeleteArtistsEvent;
import io.github.andreybgm.gigreminder.screen.artists.uievent.LoadArtistsErrorConfirmEvent;
import io.github.andreybgm.gigreminder.screen.artists.uievent.LoadArtistsEvent;
import io.github.andreybgm.gigreminder.screen.artists.uievent.OpenArtistConfirmEvent;
import io.github.andreybgm.gigreminder.screen.artists.uievent.OpenArtistsImportEvent;
import io.github.andreybgm.gigreminder.screen.artists.uievent.OpenNewArtistEvent;
import io.github.andreybgm.gigreminder.screen.artists.uievent.TurnOffActionModeEvent;
import io.github.andreybgm.gigreminder.utils.Optional;
import io.github.andreybgm.gigreminder.utils.schedulers.ImmediateSchedulerProvider;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ArtistsPresenterTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private DataSource repository;

    private ArtistsPresenter presenter;
    private ArtistsPresenter.Builder presenterBuilder;
    private List<Artist> artists;
    private Artist artist1;
    private Artist artist2;
    private RuntimeException error;
    private ArtistsUiModel loadingUiModel;
    private ArtistsUiModel loadingErrorUiModel;
    private ArtistsUiModel withArtistsUiModel;
    private ArtistsUiModel oneSelectedArtistUiModel;
    private ArtistsUiModel twoSelectedArtistsUiModel;
    private Artist artist3;

    @Before
    public void setUp() throws Exception {
        artist1 = new Artist("AR-1", "Artist1");
        artist2 = new Artist("AR-2", "Artist2");
        artist3 = new Artist("AR-3", "Artist3");
        artists = Arrays.asList(artist1, artist2, artist3);

        ImmediateSchedulerProvider schedulerProvider = new ImmediateSchedulerProvider();

        presenter = new ArtistsPresenter.Builder(
                repository, schedulerProvider)
                .build();

        presenterBuilder = new ArtistsPresenter.Builder(
                repository, schedulerProvider);

        loadingUiModel = ArtistsUiModel.Builder.create()
                .loading(true)
                .build();
        withArtistsUiModel = ArtistsUiModel.Builder.create()
                .artists(artists)
                .build();
        loadingErrorUiModel = ArtistsUiModel.Builder.create()
                .loadingError(true)
                .build();
        oneSelectedArtistUiModel = ArtistsUiModel.Builder.create()
                .artists(artists)
                .actionMode(true)
                .selectedArtistPositions(Collections.singleton(0))
                .build();
        twoSelectedArtistsUiModel = ArtistsUiModel.Builder.create()
                .artists(artists)
                .actionMode(true)
                .selectedArtistPositions(Collections.unmodifiableSet(
                        new HashSet<>(Arrays.asList(0, 1)))
                )
                .build();

        error = new RuntimeException();
    }

    @Test
    public void loadArtists() throws Exception {
        when(repository.getArtists()).thenReturn(Observable.just(artists));
        TestObserver<ArtistsUiModel> testObserver = presenter.getUiModels()
                .skip(1)
                .test();

        presenter.sendUiEvent(LoadArtistsEvent.INSTANCE);

        testObserver
                .awaitCount(2)
                .assertNoErrors()
                .assertNotComplete()
                .assertValues(loadingUiModel, withArtistsUiModel);
    }

    @Test
    public void loadArtistsWhenError() throws Exception {
        when(repository.getArtists()).thenReturn(Observable.error(error));

        // getting the error result
        presenter.sendUiEvent(LoadArtistsEvent.INSTANCE);

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(loadingErrorUiModel);

        // confirm the error showing
        presenter.sendUiEvent(LoadArtistsErrorConfirmEvent.INSTANCE);

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(ArtistsUiModel.DEFAULT);
    }

    @Test
    public void artistClickShouldOpenArtist() throws Exception {
        ArtistsPresenter presenter = presenterBuilder.uiModel(withArtistsUiModel).build();
        int position = 1;

        // open
        presenter.sendUiEvent(ArtistClickEvent.create(position));

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    Optional<Artist> artistToOpen = model.getArtistToOpen();
                    assertThat(artistToOpen.isPresent()).isTrue();
                    assertThat(artistToOpen.getValue()).isEqualTo(artist2);

                    return true;
                });

        // confirm that artist had been opened
        presenter.sendUiEvent(OpenArtistConfirmEvent.INSTANCE);

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    assertThat(model.getArtistToOpen().isPresent()).isFalse();

                    return true;
                });
    }

    @Test
    public void longArtistClickShouldTurnOnActionMode() throws Exception {
        ArtistsPresenter presenter = presenterBuilder.uiModel(withArtistsUiModel).build();
        int position = 0;

        presenter.sendUiEvent(ArtistLongClickEvent.create(position));

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    assertThat(model).isEqualTo(oneSelectedArtistUiModel);
                    assertThat(model.isActionModeOn()).isTrue();
                    assertThat(model.isArtistSelected(position)).isTrue();
                    assertThat(model.getSelectedArtistPositions()).containsOnly(position);
                    assertThat(model.getSelectedArtistIds()).containsOnly(artist1.getId());

                    return true;
                });
    }

    @Test
    public void longArtistClickWhenActionModeOnShouldDoNothing() throws Exception {
        ArtistsPresenter presenter = presenterBuilder.uiModel(oneSelectedArtistUiModel).build();

        presenter.sendUiEvent(ArtistLongClickEvent.create(1));

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(oneSelectedArtistUiModel);
    }

    @Test
    public void artistClickWhenActionModeOnShouldSelectArtist() throws Exception {
        ArtistsPresenter presenter = presenterBuilder.uiModel(oneSelectedArtistUiModel).build();

        presenter.sendUiEvent(ArtistClickEvent.create(1));

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(twoSelectedArtistsUiModel);
    }

    @Test
    public void artistClickWhenItSelectedShouldUnselect() throws Exception {
        ArtistsPresenter presenter = presenterBuilder.uiModel(twoSelectedArtistsUiModel).build();

        presenter.sendUiEvent(ArtistClickEvent.create(1));

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(oneSelectedArtistUiModel);
    }

    @Test
    public void artistClickWhenActionModeOnAndItOnlySelectedShouldTurnOffActionMode()
            throws Exception {
        ArtistsPresenter presenter = presenterBuilder.uiModel(oneSelectedArtistUiModel).build();

        presenter.sendUiEvent(ArtistClickEvent.create(0));

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(withArtistsUiModel);
    }

    @Test
    public void turnOffActionMode() throws Exception {
        ArtistsPresenter presenter = presenterBuilder.uiModel(oneSelectedArtistUiModel).build();

        presenter.sendUiEvent(TurnOffActionModeEvent.INSTANCE);

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(withArtistsUiModel);
    }

    @Test
    public void deleteSelectedArtists() throws Exception {
        when(repository.deleteArtists(any())).thenReturn(Completable.complete());
        ArtistsPresenter presenter = presenterBuilder.uiModel(twoSelectedArtistsUiModel).build();
        TestObserver<ArtistsUiModel> observer = presenter.getUiModels().skip(1).test();

        presenter.sendUiEvent(DeleteArtistsEvent.create(twoSelectedArtistsUiModel));

        verify(repository).deleteArtists(argThat(selected -> {
            assertThat(selected).hasSize(2).containsOnly(artist1, artist2);

            return true;
        }));
        observer
                .awaitCount(2)
                .assertNoErrors()
                .assertValues(
                        twoSelectedArtistsUiModel.copy()
                                .deletion(true)
                                .build(),
                        withArtistsUiModel
                );
    }

    @Test
    public void deleteSelectedArtistsWhenError() throws Exception {
        when(repository.deleteArtists(any())).thenReturn(Completable.error(new RuntimeException()));
        ArtistsPresenter presenter = presenterBuilder.uiModel(twoSelectedArtistsUiModel).build();
        ArtistsUiModel whenErrorUiModel = twoSelectedArtistsUiModel.copy()
                .deletionError(true)
                .build();
        ArtistsUiModel afterErrorShownUiModel = whenErrorUiModel.copy()
                .deletionError(false)
                .build();

        // delete
        presenter.sendUiEvent(DeleteArtistsEvent.create(twoSelectedArtistsUiModel));

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(whenErrorUiModel);

        // confirm that the error had been shown
        presenter.sendUiEvent(DeleteArtistErrorConfirmEvent.INSTANCE);

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(afterErrorShownUiModel);
    }

    @Test
    public void openImportFromGoogleMusic() throws Exception {
        // open import
        presenter.sendUiEvent(OpenArtistsImportEvent.INSTANCE);

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    assertThat(model.isShouldOpenArtistsImport()).isTrue();
                    return true;
                });

        // confirmation
        presenter.sendUiEvent(OpenArtistsImportEvent.CONFIRMATION);

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(ArtistsUiModel.DEFAULT);
    }

    @Test
    public void openNewArtist() throws Exception {
        // open new artist
        presenter.sendUiEvent(OpenNewArtistEvent.INSTANCE);

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    assertThat(model.isShouldOpenNewArtist()).isTrue();
                    return true;
                });

        // confirmation
        presenter.sendUiEvent(OpenNewArtistEvent.CONFIRMATION);

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(ArtistsUiModel.DEFAULT);
    }

    @Test
    public void createPresenterWithActionModeOn() throws Exception {
        when(repository.getArtists()).thenReturn(Observable.just(artists));
        List<String> ids = Arrays.asList(artist1.getId(), artist2.getId(), "nonexistent");
        ArtistsPresenter presenter = presenterBuilder
                .actionMode(true, ids)
                .build();

        presenter.sendUiEvent(LoadArtistsEvent.INSTANCE);

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(twoSelectedArtistsUiModel);
    }

    @Test
    public void loadArtistsWhenActionModeShouldValidateSelected() throws Exception {
        List<Artist> newArtists = Arrays.asList(artist1, artist3);
        when(repository.getArtists()).thenReturn(Observable.just(newArtists));
        ArtistsPresenter presenter = presenterBuilder.uiModel(twoSelectedArtistsUiModel).build();

        presenter.sendUiEvent(LoadArtistsEvent.INSTANCE);

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(ArtistsUiModel.Builder.create()
                        .artists(newArtists)
                        .actionMode(true)
                        .selectedArtistPositions(Collections.singleton(0))
                        .build());
    }

    @Test
    public void loadArtistsWhenNoSelectedRemainShouldTurnOffActionMode() throws Exception {
        List<Artist> newArtists = Collections.singletonList(artist3);
        when(repository.getArtists()).thenReturn(Observable.just(newArtists));
        ArtistsPresenter presenter = presenterBuilder.uiModel(twoSelectedArtistsUiModel).build();

        presenter.sendUiEvent(LoadArtistsEvent.INSTANCE);

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(ArtistsUiModel.Builder.create()
                        .artists(newArtists)
                        .build());
    }
}