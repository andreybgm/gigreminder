package io.github.andreybgm.gigreminder.screen.concertdetails;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.andreybgm.gigreminder.Injection;
import io.github.andreybgm.gigreminder.R;
import io.github.andreybgm.gigreminder.data.Concert;
import io.github.andreybgm.gigreminder.repository.RepositoryProvider;
import io.github.andreybgm.gigreminder.screen.base.RxUtils;
import io.github.andreybgm.gigreminder.screen.concertdetails.uievent.LoadConcertEvent;
import io.github.andreybgm.gigreminder.screen.concertdetails.uievent.SiteClickEvent;
import io.github.andreybgm.gigreminder.utils.ImageLoader;
import io.github.andreybgm.gigreminder.utils.Optional;
import io.github.andreybgm.gigreminder.utils.ScreenUtils;
import io.github.andreybgm.gigreminder.utils.schedulers.DefaultSchedulerProvider;
import io.reactivex.disposables.CompositeDisposable;

public class ConcertDetailsActivity extends AppCompatActivity {

    private static final String EXTRA_CONCERT_ID = "concertId";

    private ConcertDetailsPresenter presenter;
    private String dateFormatString;
    private ImageLoader imageLoader;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @BindView(R.id.screen_concert_details)
    View rootView;

    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;

    @BindView(R.id.backdrop)
    ImageView backdropImageView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.text_artist)
    TextView artistTextView;

    @BindView(R.id.text_place)
    TextView placeTextView;

    @BindView(R.id.text_date)
    TextView dateTextView;

    @BindView(R.id.content_data)
    View contentDataView;

    @BindView(R.id.progress_loading)
    View loadingProgressView;

    @BindView(R.id.text_open_site)
    View openSiteView;

    @NonNull
    public static Intent makeIntent(@NonNull Context context, @NonNull Concert concert) {
        Intent intent = new Intent(context, ConcertDetailsActivity.class);
        intent.putExtra(EXTRA_CONCERT_ID, concert.getId());

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_concert_details);
        ButterKnife.bind(this);

        String concertId = getIntent().getStringExtra(EXTRA_CONCERT_ID);

        //noinspection unchecked
        presenter = (ConcertDetailsPresenter) getLastCustomNonConfigurationInstance();

        if (presenter == null) {
            presenter = new ConcertDetailsPresenter.Builder(
                    RepositoryProvider.provideRepository(getApplicationContext()),
                    DefaultSchedulerProvider.getInstance())
                    .concertId(concertId)
                    .build();
            presenter.sendUiEvent(LoadConcertEvent.INSTANCE);
        }

        toolbar.setNavigationIcon(R.drawable.ic_menu_back);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        dateFormatString = DateFormat.getBestDateTimePattern(Locale.getDefault(), "yyyyMMMMdEEEE");
        imageLoader = Injection.provideInjection().provideImageLoader();
        openSiteView.setOnClickListener(v -> presenter.sendUiEvent(SiteClickEvent.INSTANCE));
    }

    @Override
    protected void onStart() {
        super.onStart();

        compositeDisposable.add(
                presenter.getUiModels()
                        .compose(RxUtils.observeOnUi())
                        .subscribe(this::acceptUiModel)
        );
    }

    @Override
    protected void onStop() {
        super.onStop();

        compositeDisposable.clear();
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return presenter;
    }

    private void acceptUiModel(ConcertDetailsUiModel newModel) {
        if (!newModel.getLinkToOpen().isEmpty()) {
            openLink(newModel.getLinkToOpen());
            presenter.sendUiEvent(SiteClickEvent.CONFIRMATION);
            return;
        }

        if (newModel.isLoadingError()) {
            Toast.makeText(this, R.string.error_loading_data, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Set<View> allViews = new HashSet<>();
        allViews.add(contentDataView);
        allViews.add(loadingProgressView);

        Set<View> visibleViews = new HashSet<>();

        if (newModel.isLoading()) {
            visibleViews.add(loadingProgressView);
        } else {
            fillViews(newModel.getConcert());
            visibleViews.add(contentDataView);
        }

        ScreenUtils.remainVisibleViews(allViews, visibleViews);
        requestLayout();
    }

    private void fillViews(Optional<Concert> optional) {
        if (!optional.isPresent()) {
            return;
        }

        Concert concert = optional.getValue();
        String artistName = concert.getArtist().getName();

        collapsingToolbar.setTitle(artistName);
        collapsingToolbar.setExpandedTitleColor(
                ContextCompat.getColor(this, android.R.color.transparent));
        artistTextView.setText(artistName);

        String placeText = String.format("%s, %s",
                concert.getLocation().getName(), concert.getPlace());
        placeTextView.setText(placeText);

        dateTextView.setText(makeDateString(concert));

        imageLoader.loadConcertBackdrop(this, concert, backdropImageView);
    }

    private CharSequence makeDateString(Concert concert) {
        GregorianCalendar concertCalendar = new GregorianCalendar();
        concertCalendar.setTime(concert.getDate());

        return DateFormat.format(dateFormatString, concert.getDate());
    }

    private void openLink(@NonNull String url) {
        if (url.isEmpty()) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void requestLayout() {
        ScreenUtils.postRequestLayout(rootView);
    }
}
