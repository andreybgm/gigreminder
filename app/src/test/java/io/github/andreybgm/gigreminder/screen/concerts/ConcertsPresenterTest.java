package io.github.andreybgm.gigreminder.screen.concerts;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import io.github.andreybgm.gigreminder.data.Artist;
import io.github.andreybgm.gigreminder.data.Concert;
import io.github.andreybgm.gigreminder.data.Location;
import io.github.andreybgm.gigreminder.repository.DataSource;
import io.github.andreybgm.gigreminder.repository.sync.eventbus.SyncEventBus;
import io.github.andreybgm.gigreminder.repository.sync.eventbus.SyncFinishEvent;
import io.github.andreybgm.gigreminder.repository.sync.eventbus.SyncStartEvent;
import io.github.andreybgm.gigreminder.screen.concerts.uievent.ConcertClickEvent;
import io.github.andreybgm.gigreminder.screen.concerts.uievent.LoadConcertsErrorConfirmEvent;
import io.github.andreybgm.gigreminder.screen.concerts.uievent.LoadConcertsEvent;
import io.github.andreybgm.gigreminder.screen.concerts.uievent.OpenConcertConfirmEvent;
import io.github.andreybgm.gigreminder.utils.Optional;
import io.github.andreybgm.gigreminder.utils.schedulers.ImmediateSchedulerProvider;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ConcertsPresenterTest {
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private DataSource repository;
    private ConcertsPresenter presenter;
    private List<Concert> concerts;
    private List<Concert> sortedConcerts;
    private ConcertsPresenter.Builder presenterBuilder;
    private ConcertsUiModel withConcertsUiModel;

    @Before
    public void setUp() throws Exception {
        ImmediateSchedulerProvider schedulerProvider = new ImmediateSchedulerProvider();
        ConcertsPresenter.CurrentTimeProvider currentTimeProvider =
                () -> (new GregorianCalendar(2017, 0, 3, 22, 0)).getTime();

        presenter = new ConcertsPresenter.Builder(repository, schedulerProvider)
                .currentTimeProvider(currentTimeProvider)
                .build();

        presenterBuilder = new ConcertsPresenter.Builder(repository, schedulerProvider)
                .currentTimeProvider(currentTimeProvider);

        Location location = new Location("LC-1", "lc1", "Location1");
        Artist artist = new Artist("AR-1", "Artist1");
        Concert concert1 = new Concert.Builder("CN1", "1001", artist, location)
                .date((new GregorianCalendar(2017, 0, 1, 20, 30)).getTime())
                .place("Place2001")
                .imageUrl("http://example.com/img1001.jpg")
                .url("http://example.com/events/1001")
                .build();
        Concert concert2 = new Concert.Builder("CN2", "1002", artist, location)
                .date((new GregorianCalendar(2017, 0, 2, 20, 30)).getTime())
                .place("Place2002")
                .imageUrl("http://example.com/img1002.jpg")
                .url("http://example.com/events/1002")
                .build();
        Concert concert3 = new Concert.Builder("CN3", "1003", artist, location)
                .date((new GregorianCalendar(2017, 0, 3, 20, 30)).getTime())
                .place("Place2003")
                .imageUrl("http://example.com/img1003.jpg")
                .url("http://example.com/events/1003")
                .build();
        Concert concert4 = new Concert.Builder("CN4", "1004", artist, location)
                .date((new GregorianCalendar(2017, 0, 4, 20, 30)).getTime())
                .place("Place2004")
                .imageUrl("http://example.com/img1004.jpg")
                .url("http://example.com/events/1004")
                .build();
        Concert concert5 = new Concert.Builder("CN5", "1005", artist, location)
                .date((new GregorianCalendar(2018, 0, 1, 20, 30)).getTime())
                .place("Place2005")
                .imageUrl("http://example.com/img1005.jpg")
                .url("http://example.com/events/1005")
                .build();
        concerts = Arrays.asList(concert1, concert2, concert3, concert4, concert5);
        sortedConcerts = Arrays.asList(
                concert3, concert4, concert5, concert1, concert2);

        withConcertsUiModel = ConcertsUiModel.Builder.create()
                .concerts(sortedConcerts)
                .build();
    }

    @Test
    public void loadConcerts() throws Exception {
        when(repository.getConcerts()).thenReturn(Observable.just(concerts));
        TestObserver<ConcertsUiModel> testObserver = presenter.getUiModels()
                .skip(1)
                .test();

        presenter.sendUiEvent(LoadConcertsEvent.INSTANCE);

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
                    assertThat(model.getConcerts()).isEqualTo(sortedConcerts);

                    return true;
                });
    }

    @Test
    public void loadConcertsWhenError() throws Exception {
        when(repository.getConcerts()).thenReturn(Observable.error(new RuntimeException()));

        // show error
        presenter.sendUiEvent(LoadConcertsEvent.INSTANCE);

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertNotComplete()
                .assertValue(model -> {
                    assertThat(model.isLoading()).isFalse();
                    assertThat(model.isLoadingError()).isTrue();
                    assertThat(model.getConcerts()).isEmpty();

                    return true;
                });

        // confirm
        presenter.sendUiEvent(LoadConcertsErrorConfirmEvent.INSTANCE);

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
    public void handleArtistClick() throws Exception {
        ConcertsPresenter presenter = presenterBuilder.uiModel(withConcertsUiModel).build();
        int position = 1;
        Concert concert = withConcertsUiModel.getConcerts().get(position);

        // open
        presenter.sendUiEvent(ConcertClickEvent.create(position));

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    Optional<Concert> concertToOpen = model.getConcertToOpen();
                    assertThat(concertToOpen.isPresent()).isTrue();
                    assertThat(concertToOpen.getValue()).isEqualTo(concert);

                    return true;
                });

        // confirm
        presenter.sendUiEvent(OpenConcertConfirmEvent.INSTANCE);

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    Optional<Concert> concertToOpen = model.getConcertToOpen();
                    assertThat(concertToOpen.isPresent()).isFalse();

                    return true;
                });
    }

    @Test
    public void handleSyncingEvents() throws Exception {
        // start sync
        SyncEventBus.sendEvent(SyncStartEvent.create());

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    assertThat(model.isSyncing()).isTrue();

                    return true;
                });

        // finish sync
        SyncEventBus.sendEvent(SyncFinishEvent.create());

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    assertThat(model.isSyncing()).isFalse();

                    return true;
                });
    }
}