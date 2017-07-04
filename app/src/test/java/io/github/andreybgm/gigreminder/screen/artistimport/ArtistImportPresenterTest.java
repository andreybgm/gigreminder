package io.github.andreybgm.gigreminder.screen.artistimport;

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

import io.github.andreybgm.gigreminder.R;
import io.github.andreybgm.gigreminder.repository.DataSource;
import io.github.andreybgm.gigreminder.screen.artistimport.uievent.ArtistClickEvent;
import io.github.andreybgm.gigreminder.screen.artistimport.uievent.LoadArtistsErrorConfirmEvent;
import io.github.andreybgm.gigreminder.screen.artistimport.uievent.LoadArtistsEvent;
import io.github.andreybgm.gigreminder.screen.artistimport.uievent.SaveArtistsErrorConfirmEvent;
import io.github.andreybgm.gigreminder.screen.artistimport.uievent.SaveArtistsEvent;
import io.github.andreybgm.gigreminder.screen.artistimport.uievent.SelectAllEvent;
import io.github.andreybgm.gigreminder.screen.artistimport.uievent.UnselectAllEvent;
import io.github.andreybgm.gigreminder.utils.Optional;
import io.github.andreybgm.gigreminder.utils.schedulers.ImmediateSchedulerProvider;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ArtistImportPresenterTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private DataSource repository;

    private ArtistImportPresenter presenter;
    private ArtistImportPresenter.Builder presenterBuilder;
    private List<String> allNames;
    private ArtistImportUiModel withArtistsUiModel;
    private ArtistImportUiModel oneSelectedArtistUiModel;
    private ArtistImportUiModel twoSelectedArtistsUiModel;

    @Before
    public void setUp() throws Exception {
        allNames = Arrays.asList("Artist1", "Artist2", "Artist3", "Artist4", "Artist5");

        ImmediateSchedulerProvider schedulerProvider = new ImmediateSchedulerProvider();

        presenter = new ArtistImportPresenter.Builder(
                repository, schedulerProvider)
                .firstLoading(true)
                .build();
        presenterBuilder = new ArtistImportPresenter.Builder(
                repository, schedulerProvider);

        withArtistsUiModel = ArtistImportUiModel.DEFAULT.copy()
                .artists(allNames)
                .build();
        oneSelectedArtistUiModel = ArtistImportUiModel.DEFAULT.copy()
                .artists(allNames)
                .selectedArtistPositions(Collections.singleton(0))
                .build();
        twoSelectedArtistsUiModel = ArtistImportUiModel.DEFAULT.copy()
                .artists(allNames)
                .selectedArtistPositions(Collections.unmodifiableSet(
                        new HashSet<>(Arrays.asList(0, 1)))
                )
                .build();
    }

    @Test
    public void loadArtists() throws Exception {
        when(repository.loadArtistsFromGoogleMusic()).thenReturn(Single.just(allNames));
        TestObserver<ArtistImportUiModel> testObserver = presenter.getUiModels()
                .skip(1)
                .test();

        presenter.sendUiEvent(LoadArtistsEvent.INSTANCE);

        testObserver
                .awaitCount(2)
                .assertNoErrors()
                .assertNotComplete()
                .assertValueAt(0, model -> {
                    assertThat(model.isLoading()).isTrue();

                    return true;
                })
                .assertValueAt(1, model -> {
                    assertThat(model.isLoading()).isFalse();
                    assertThat(model.isLoadingError()).isFalse();
                    assertThat(model.isFirstLoading()).isFalse();
                    assertThat(model.getArtists()).isEqualTo(allNames);
                    assertThat(model.getSelectedArtistPositions())
                            .hasSize(5)
                            .containsAll(
                                    Observable.range(0, allNames.size()).blockingIterable()
                            );

                    return true;
                });
    }

    @Test
    public void loadArtistsWhenError() throws Exception {
        when(repository.loadArtistsFromGoogleMusic()).thenReturn(Single.error(
                new RuntimeException()));

        // show error
        presenter.sendUiEvent(LoadArtistsEvent.INSTANCE);

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    assertThat(model.isLoadingError()).isTrue();
                    assertThat(model.isFirstLoading()).isTrue();

                    return true;
                });

        // confirm
        presenter.sendUiEvent(LoadArtistsErrorConfirmEvent.INSTANCE);

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    assertThat(model.isLoadingError()).isFalse();

                    return true;
                });
    }

    @Test
    public void loadArtistWhenInitialSelectedExist() throws Exception {
        when(repository.loadArtistsFromGoogleMusic()).thenReturn(Single.just(allNames));
        ArtistImportPresenter presenter = presenterBuilder
                .firstLoading(false)
                .selectedArtistNames(Optional.of(
                        Arrays.asList("Nonexistent", "Artist1", "Artist2"))
                )
                .build();

        presenter.sendUiEvent(LoadArtistsEvent.INSTANCE);

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    assertThat(model.getSelectedArtistPositions())
                            .hasSize(2)
                            .contains(0, 1);

                    return true;
                });
    }

    @Test
    public void artistClickShouldSelectArtist() throws Exception {
        ArtistImportPresenter presenter = presenterBuilder.uiModel(oneSelectedArtistUiModel)
                .build();

        presenter.sendUiEvent(ArtistClickEvent.create(1));

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(twoSelectedArtistsUiModel);
    }

    @Test
    public void artistClickWhenItSelectedShouldUnselect() throws Exception {
        ArtistImportPresenter presenter = presenterBuilder.uiModel(twoSelectedArtistsUiModel)
                .build();

        presenter.sendUiEvent(ArtistClickEvent.create(1));

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(oneSelectedArtistUiModel);
    }

    @Test
    public void selectAll() throws Exception {
        Iterable<Integer> expectedSelectedPositions = Observable.range(0, allNames.size())
                .blockingIterable();
        ArtistImportPresenter presenter = presenterBuilder.uiModel(withArtistsUiModel).build();

        presenter.sendUiEvent(SelectAllEvent.INSTANCE);

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    assertThat(model.getSelectedArtistPositions())
                            .hasSize(5)
                            .containsAll(expectedSelectedPositions);

                    return true;
                });
    }

    @Test
    public void unselectAll() throws Exception {
        ArtistImportPresenter presenter = presenterBuilder.uiModel(twoSelectedArtistsUiModel)
                .build();

        presenter.sendUiEvent(UnselectAllEvent.INSTANCE);

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    assertThat(model.getSelectedArtistPositions()).isEmpty();

                    return true;
                });
    }

    @Test
    public void saveSelectedArtists() throws Exception {
        when(repository.saveArtists(any())).thenReturn(Completable.complete());
        ArtistImportPresenter presenter = presenterBuilder.uiModel(twoSelectedArtistsUiModel)
                .build();
        TestObserver<ArtistImportUiModel> observer = presenter.getUiModels()
                .skip(1)
                .test();

        presenter.sendUiEvent(SaveArtistsEvent.create(twoSelectedArtistsUiModel));

        observer
                .awaitCount(2)
                .assertNoErrors()
                .assertValueAt(0, model -> {
                    assertThat(model.isSaving()).isTrue();

                    return true;
                })
                .assertValueAt(1, model -> {
                    assertThat(model.isSaving()).isFalse();
                    assertThat(model.getSavingError().isPresent()).isFalse();
                    assertThat(model.isShouldClose()).isTrue();

                    return true;
                });
    }

    @Test
    public void saveSelectedArtistsWhenError() throws Exception {
        when(repository.saveArtists(any())).thenReturn(Completable.error(new RuntimeException()));
        ArtistImportPresenter presenter = presenterBuilder.uiModel(twoSelectedArtistsUiModel)
                .build();

        // show error
        presenter.sendUiEvent(SaveArtistsEvent.create(twoSelectedArtistsUiModel));

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    assertThat(model.isSaving()).isFalse();
                    assertThat(model.getSavingError().isPresent()).isTrue();
                    assertThat(model.getSavingError().getValue())
                            .isEqualTo(R.string.error_saving_data);
                    assertThat(model.isShouldClose()).isFalse();

                    return true;
                });

        // confirm
        presenter.sendUiEvent(SaveArtistsErrorConfirmEvent.INSTANCE);

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    assertThat(model.getSavingError().isPresent()).isFalse();

                    return true;
                });
    }

    @Test
    public void saveSelectedArtistsWhenThereAreNoSelectedItems() throws Exception {
        ArtistImportPresenter presenter = presenterBuilder.uiModel(withArtistsUiModel).build();

        presenter.sendUiEvent(SaveArtistsEvent.create(withArtistsUiModel));

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    assertThat(model.isSaving()).isFalse();
                    assertThat(model.getSavingError().isPresent()).isTrue();
                    assertThat(model.getSavingError().getValue())
                            .isEqualTo(R.string.artist_import_error_no_artists_to_save);
                    assertThat(model.isShouldClose()).isFalse();

                    return true;
                });
    }
}