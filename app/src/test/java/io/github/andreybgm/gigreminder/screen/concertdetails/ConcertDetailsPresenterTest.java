package io.github.andreybgm.gigreminder.screen.concertdetails;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.GregorianCalendar;

import io.github.andreybgm.gigreminder.data.Artist;
import io.github.andreybgm.gigreminder.data.Concert;
import io.github.andreybgm.gigreminder.data.Location;
import io.github.andreybgm.gigreminder.repository.DataSource;
import io.github.andreybgm.gigreminder.screen.concertdetails.uievent.LoadConcertEvent;
import io.github.andreybgm.gigreminder.screen.concertdetails.uievent.SiteClickEvent;
import io.github.andreybgm.gigreminder.utils.schedulers.ImmediateSchedulerProvider;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ConcertDetailsPresenterTest {
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private DataSource repository;

    private ConcertDetailsPresenter presenter;
    private ConcertDetailsPresenter.Builder presenterBuilder;
    private Concert concert;
    private ConcertDetailsUiModel loadedUiModel;

    @Before
    public void setUp() throws Exception {
        String concertId = "CN-1-1";
        presenter = new ConcertDetailsPresenter.Builder(
                repository, new ImmediateSchedulerProvider())
                .concertId(concertId)
                .build();
        presenterBuilder = new ConcertDetailsPresenter.Builder(
                repository, new ImmediateSchedulerProvider())
                .concertId(concertId);
        Location location = new Location("LC-1", "lc1", "Location1");
        Artist artist = new Artist("AR-1", "Artist1");
        concert = new Concert.Builder(concertId, "1001", artist, location)
                .date((new GregorianCalendar(2017, 1, 1, 20, 30)).getTime())
                .place("Place2001")
                .imageUrl("http://github.com/img1001.jpg")
                .url("http://github.com/events/1001")
                .build();
        loadedUiModel = ConcertDetailsUiModel.createDefault(concertId).copy()
                .concert(concert)
                .build();
    }

    @Test
    public void loadConcert() throws Exception {
        when(repository.getConcert(concert.getId())).thenReturn(Single.just(concert));
        TestObserver<ConcertDetailsUiModel> observer = presenter.getUiModels()
                .skip(1)
                .test();

        presenter.sendUiEvent(LoadConcertEvent.INSTANCE);

        observer
                .awaitCount(2)
                .assertNoErrors()
                .assertValueAt(0, model -> {
                    assertThat(model.isLoading()).isTrue();

                    return true;
                })
                .assertValueAt(1, model -> {
                    assertThat(model.isLoading()).isFalse();
                    assertThat(model.getConcert().isPresent()).isTrue();
                    assertThat(model.getConcert().getValue()).isEqualTo(concert);

                    return true;
                });
    }

    @Test
    public void loadConcertWhenError() throws Exception {
        when(repository.getConcert(concert.getId())).thenReturn(Single.error(
                new RuntimeException()));

        presenter.sendUiEvent(LoadConcertEvent.INSTANCE);

        presenter.getUiModels()
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
    public void handleSiteClick() throws Exception {
        ConcertDetailsPresenter presenter = presenterBuilder.uiModel(loadedUiModel).build();

        // open link
        presenter.sendUiEvent(SiteClickEvent.INSTANCE);

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(model -> {
                    assertThat(model.getLinkToOpen()).isEqualTo(concert.getUrl());

                    return true;
                });

        // confirm
        presenter.sendUiEvent(SiteClickEvent.CONFIRMATION);

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(loadedUiModel);
    }
}