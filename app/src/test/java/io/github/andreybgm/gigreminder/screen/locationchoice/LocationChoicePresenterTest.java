package io.github.andreybgm.gigreminder.screen.locationchoice;

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
import io.github.andreybgm.gigreminder.screen.locationchoice.uievent.LoadLocationsErrorConfirmEvent;
import io.github.andreybgm.gigreminder.screen.locationchoice.uievent.LoadLocationsEvent;
import io.github.andreybgm.gigreminder.screen.locationchoice.uievent.LocationClickEvent;
import io.github.andreybgm.gigreminder.utils.Optional;
import io.github.andreybgm.gigreminder.utils.schedulers.ImmediateSchedulerProvider;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class LocationChoicePresenterTest {
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private DataSource repository;

    private Location location1;
    private List<Location> locations;
    private LocationChoicePresenter presenter;
    private LocationChoicePresenter.Builder presenterBuilder;
    private LocationChoiceUiModel withLocationsUiModel;

    @Before
    public void setUp() throws Exception {
        ImmediateSchedulerProvider schedulerProvider = new ImmediateSchedulerProvider();

        presenter = new LocationChoicePresenter.Builder(repository, schedulerProvider).build();
        presenterBuilder = new LocationChoicePresenter.Builder(repository, schedulerProvider);

        location1 = new Location("lc1", "Location1");
        Location location2 = new Location("lc2", "Location2");
        Location location3 = new Location("lc3", "Location3");
        locations = Arrays.asList(location1, location2, location3);

        withLocationsUiModel = LocationChoiceUiModel.Builder.create()
                .locations(locations)
                .build();
    }

    @Test
    public void loadLocations() throws Exception {
        when(repository.getAvailableLocations()).thenReturn(Single.just(locations));
        TestObserver<LocationChoiceUiModel> testObserver = presenter.getUiModels()
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
        when(repository.getAvailableLocations()).thenReturn(Single.error(
                new RuntimeException()));

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
    public void handleLocationClick() throws Exception {
        LocationChoicePresenter presenter = presenterBuilder.uiModel(withLocationsUiModel).build();
        int position = 0;
        Location location = withLocationsUiModel.getLocations().get(position);

        presenter.sendUiEvent(LocationClickEvent.create(position));

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    Optional<Location> locationToOpen = model.getLocationToOpen();
                    assertThat(locationToOpen.isPresent()).isTrue();
                    assertThat(locationToOpen.getValue()).isEqualTo(location);

                    return true;
                });
    }
}