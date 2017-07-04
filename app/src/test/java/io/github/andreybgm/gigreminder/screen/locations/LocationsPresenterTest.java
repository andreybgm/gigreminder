package io.github.andreybgm.gigreminder.screen.locations;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Arrays;
import java.util.List;

import io.github.andreybgm.gigreminder.data.Location;
import io.github.andreybgm.gigreminder.repository.DataSource;
import io.github.andreybgm.gigreminder.screen.locations.uievent.DeleteLocationErrorConfirmEvent;
import io.github.andreybgm.gigreminder.screen.locations.uievent.DeleteLocationEvent;
import io.github.andreybgm.gigreminder.screen.locations.uievent.LoadLocationsErrorConfirmEvent;
import io.github.andreybgm.gigreminder.screen.locations.uievent.LoadLocationsEvent;
import io.github.andreybgm.gigreminder.screen.locations.uievent.OpenNewLocationEvent;
import io.github.andreybgm.gigreminder.screen.locations.uievent.SaveLocationErrorConfirmEvent;
import io.github.andreybgm.gigreminder.screen.locations.uievent.SaveLocationEvent;
import io.github.andreybgm.gigreminder.utils.schedulers.ImmediateSchedulerProvider;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LocationsPresenterTest {
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private DataSource repository;

    private Location location1;
    private List<Location> locations;
    private LocationsPresenter presenter;
    private LocationsPresenter.Builder presenterBuilder;
    private LocationsUiModel withLocationsUiModel;

    @Before
    public void setUp() throws Exception {
        ImmediateSchedulerProvider schedulerProvider = new ImmediateSchedulerProvider();

        presenter = new LocationsPresenter.Builder(repository, schedulerProvider).build();
        presenterBuilder = new LocationsPresenter.Builder(repository, schedulerProvider);

        location1 = new Location("lc1", "Location1");
        Location location2 = new Location("lc2", "Location2");
        Location location3 = new Location("lc3", "Location3");
        locations = Arrays.asList(location1, location2, location3);

        withLocationsUiModel = LocationsUiModel.Builder.create()
                .locations(locations)
                .build();
    }

    @Test
    public void loadLocations() throws Exception {
        when(repository.getLocations()).thenReturn(Observable.just(locations));
        TestObserver<LocationsUiModel> testObserver = presenter.getUiModels()
                .skip(1)
                .test();

        presenter.sendUiEvent(LoadLocationsEvent.INSTANCE);

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
                    assertThat(model.getLocations()).isEqualTo(locations);

                    return true;
                });
    }

    @Test
    public void loadLocationsWhenError() throws Exception {
        when(repository.getLocations()).thenReturn(Observable.error(new RuntimeException()));

        // show error
        presenter.sendUiEvent(LoadLocationsEvent.INSTANCE);

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertNotComplete()
                .assertValue(model -> {
                    assertThat(model.isLoading()).isFalse();
                    assertThat(model.isLoadingError()).isTrue();
                    assertThat(model.getLocations()).isEmpty();

                    return true;
                });

        // confirm
        presenter.sendUiEvent(LoadLocationsErrorConfirmEvent.INSTANCE);

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertNotComplete()
                .assertValue(model -> {
                    assertThat(model.isLoadingError()).isFalse();

                    return true;
                });
    }

    @Test
    public void deleteLocation() throws Exception {
        when(repository.deleteLocation(location1)).thenReturn(Completable.complete());
        LocationsPresenter presenter = presenterBuilder.uiModel(withLocationsUiModel).build();
        TestObserver<LocationsUiModel> observer = presenter.getUiModels()
                .skip(1)
                .test();
        int position = 0;
        Location location = withLocationsUiModel.getLocations().get(position);

        presenter.sendUiEvent(DeleteLocationEvent.create(position, withLocationsUiModel));

        verify(repository).deleteLocation(location);
        observer
                .awaitCount(2)
                .assertNoErrors()
                .assertValueAt(0, model -> {
                    assertThat(model.isDeletion()).isTrue();

                    return true;
                })
                .assertValueAt(1, model -> {
                    assertThat(model.isDeletion()).isFalse();
                    assertThat(model.isDeletionError()).isFalse();

                    return true;
                });
    }

    @Test
    public void deleteLocationWhenError() throws Exception {
        when(repository.deleteLocation(any())).thenReturn(Completable.error(
                new RuntimeException()));
        LocationsPresenter presenter = presenterBuilder.uiModel(withLocationsUiModel).build();

        // get an error
        presenter.sendUiEvent(DeleteLocationEvent.create(0, withLocationsUiModel));

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    assertThat(model.isDeletion()).isFalse();
                    assertThat(model.isDeletionError()).isTrue();

                    return true;
                });

        // confirm
        presenter.sendUiEvent(DeleteLocationErrorConfirmEvent.INSTANCE);

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    assertThat(model.isDeletionError()).isFalse();

                    return true;
                });
    }

    @Test
    public void saveLocation() throws Exception {
        when(repository.saveLocation(location1)).thenReturn(Completable.complete());
        TestObserver<LocationsUiModel> observer = presenter.getUiModels()
                .skip(1)
                .test();

        presenter.sendUiEvent(SaveLocationEvent.create(location1));

        verify(repository).saveLocation(location1);
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

                    return true;
                });
    }

    @Test
    public void saveLocationWhenError() throws Exception {
        when(repository.saveLocation(any())).thenReturn(Completable.error(
                new RuntimeException()));

        // get an error
        presenter.sendUiEvent(SaveLocationEvent.create(location1));

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    assertThat(model.isSaving()).isFalse();
                    assertThat(model.isSavingError()).isTrue();

                    return true;
                });

        // confirm
        presenter.sendUiEvent(SaveLocationErrorConfirmEvent.INSTANCE);

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
    public void openNewLocation() throws Exception {
        // open
        presenter.sendUiEvent(OpenNewLocationEvent.INSTANCE);

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    assertThat(model.isShouldOpenNewLocation()).isTrue();

                    return true;
                });

        // confirm
        presenter.sendUiEvent(OpenNewLocationEvent.CONFIRMATION);

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    assertThat(model.isShouldOpenNewLocation()).isFalse();

                    return true;
                });
    }
}