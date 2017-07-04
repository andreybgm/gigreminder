package io.github.andreybgm.gigreminder.screen.artistimport;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.andreybgm.gigreminder.R;
import io.github.andreybgm.gigreminder.repository.RepositoryProvider;
import io.github.andreybgm.gigreminder.screen.artistimport.uievent.ArtistClickEvent;
import io.github.andreybgm.gigreminder.screen.artistimport.uievent.LoadArtistsEvent;
import io.github.andreybgm.gigreminder.screen.artistimport.uievent.SaveArtistsErrorConfirmEvent;
import io.github.andreybgm.gigreminder.screen.artistimport.uievent.SaveArtistsEvent;
import io.github.andreybgm.gigreminder.screen.artistimport.uievent.SelectAllEvent;
import io.github.andreybgm.gigreminder.screen.artistimport.uievent.UnselectAllEvent;
import io.github.andreybgm.gigreminder.screen.base.RxUtils;
import io.github.andreybgm.gigreminder.utils.Optional;
import io.github.andreybgm.gigreminder.utils.ScreenUtils;
import io.github.andreybgm.gigreminder.utils.schedulers.DefaultSchedulerProvider;
import io.reactivex.disposables.CompositeDisposable;

public class ArtistImportActivity extends AppCompatActivity implements
        Toolbar.OnMenuItemClickListener {

    public static final String STATE_SELECTED_NAMES = "SELECTED_NAMES";

    @BindView(R.id.screen_import_artists)
    View rootView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.list_names)
    RecyclerView namesView;

    @BindView(R.id.empty_list)
    View emptyListView;

    @BindView(R.id.progress_loading)
    View loadingProgressView;

    @BindView(R.id.loading_error)
    View loadingErrorView;

    @BindView(R.id.button_retry_import_artists)
    Button retryButton;

    private ListAdapter adapter;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private ArtistImportPresenter presenter;
    @NonNull
    private ArtistImportUiModel uiModel = ArtistImportUiModel.DEFAULT;

    public static Intent makeIntent(Context context) {
        return new Intent(context, ArtistImportActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_artist_import);
        ButterKnife.bind(this);

        presenter = (ArtistImportPresenter) getLastCustomNonConfigurationInstance();

        if (presenter == null) {
            boolean firstLoading;
            Optional<List<String>> selectedNames;

            if (savedInstanceState == null) {
                firstLoading = true;
                selectedNames = Optional.empty();
            } else {
                firstLoading = false;
                ArrayList<String> value = savedInstanceState.getStringArrayList(
                        STATE_SELECTED_NAMES);
                selectedNames = Optional.of(value == null ? Collections.emptyList() : value);
            }

            presenter = new ArtistImportPresenter.Builder(
                    RepositoryProvider.provideRepository(getApplicationContext()),
                    DefaultSchedulerProvider.getInstance())
                    .firstLoading(firstLoading)
                    .selectedArtistNames(selectedNames)
                    .build();
            presenter.sendUiEvent(LoadArtistsEvent.INSTANCE);
        }

        toolbar.setTitle(R.string.artist_import_title);
        toolbar.setNavigationIcon(R.drawable.ic_menu_discard);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.inflateMenu(R.menu.activity_importartists);
        toolbar.setOnMenuItemClickListener(this);

        adapter = new ListAdapter(this, uiModel);
        adapter.setOnItemClickListener(position -> presenter.sendUiEvent(
                ArtistClickEvent.create(position))
        );

        namesView.setHasFixedSize(true);
        namesView.setAdapter(adapter);
        namesView.setLayoutManager(new LinearLayoutManager(this));
        namesView.addItemDecoration(new DividerItemDecoration(this,
                LinearLayoutManager.VERTICAL));

        retryButton.setOnClickListener(v -> presenter.sendUiEvent(LoadArtistsEvent.INSTANCE));
    }

    @Override
    public void onStart() {
        super.onStart();

        compositeDisposable.add(
                presenter.getUiModels()
                        .compose(RxUtils.observeOnUi())
                        .subscribe(this::acceptNewUiModel)
        );
    }

    @Override
    public void onStop() {
        super.onStop();

        compositeDisposable.clear();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putStringArrayList(STATE_SELECTED_NAMES,
                new ArrayList<>(uiModel.getSelectedArtists()));
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return presenter;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_import:
                presenter.sendUiEvent(SaveArtistsEvent.create(uiModel));
                return true;
            case R.id.menu_check_all:
                presenter.sendUiEvent(SelectAllEvent.INSTANCE);
                return true;
            case R.id.menu_check_nothing:
                presenter.sendUiEvent(UnselectAllEvent.INSTANCE);
                return true;
        }

        return false;
    }

    private void acceptNewUiModel(ArtistImportUiModel newModel) {
        if (newModel.isShouldClose()) {
            finish();
            return;
        }

        if (newModel.getSavingError().isPresent()) {
            showError(newModel.getSavingError().getValue());
            presenter.sendUiEvent(SaveArtistsErrorConfirmEvent.INSTANCE);
            return;
        }

        uiModel = newModel;
        adapter.changeDataSet(newModel);

        Set<View> allViews = new HashSet<>();
        allViews.add(namesView);
        allViews.add(emptyListView);
        allViews.add(loadingProgressView);
        allViews.add(loadingErrorView);

        Set<View> visibleViews = new HashSet<>();

        if (newModel.isLoading()) {
            visibleViews.add(loadingProgressView);
        } else if (newModel.isLoadingError()) {
            visibleViews.add(loadingErrorView);
        } else if (newModel.getArtists().isEmpty()) {
            visibleViews.add(emptyListView);
        } else {
            visibleViews.add(namesView);
        }

        ScreenUtils.remainVisibleViews(allViews, visibleViews);
        requestLayout();
    }

    private void showError(int msg) {
        Snackbar.make(rootView, msg, Snackbar.LENGTH_LONG).show();
    }

    private void requestLayout() {
        ScreenUtils.postRequestLayout(rootView);
    }
}
