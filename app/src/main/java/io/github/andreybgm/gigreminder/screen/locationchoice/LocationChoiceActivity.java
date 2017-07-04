package io.github.andreybgm.gigreminder.screen.locationchoice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.gson.Gson;

import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.andreybgm.gigreminder.R;
import io.github.andreybgm.gigreminder.data.Location;
import io.github.andreybgm.gigreminder.repository.RepositoryProvider;
import io.github.andreybgm.gigreminder.screen.base.RxUtils;
import io.github.andreybgm.gigreminder.screen.locationchoice.uievent.LoadLocationsErrorConfirmEvent;
import io.github.andreybgm.gigreminder.screen.locationchoice.uievent.LoadLocationsEvent;
import io.github.andreybgm.gigreminder.screen.locationchoice.uievent.LocationClickEvent;
import io.github.andreybgm.gigreminder.utils.ListViewHelper;
import io.github.andreybgm.gigreminder.utils.ScreenUtils;
import io.github.andreybgm.gigreminder.utils.schedulers.DefaultSchedulerProvider;
import io.reactivex.disposables.CompositeDisposable;

public class LocationChoiceActivity extends AppCompatActivity {

    public static final String EXTRA_LOCATION = "LOCATION";

    @BindView(R.id.screen_location_choice)
    View rootView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.list_locations)
    RecyclerView locationsView;

    @BindView(R.id.empty_list)
    View emptyListView;

    @BindView(R.id.progress_loading)
    View loadingProgressView;

    private LocationChoicePresenter presenter;
    private ListAdapter adapter;
    private ListViewHelper listHelper;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public static Intent makeIntent(Context context) {
        return new Intent(context, LocationChoiceActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_location_choice);

        ButterKnife.bind(this);

        presenter = (LocationChoicePresenter) getLastCustomNonConfigurationInstance();

        if (presenter == null) {
            presenter = new LocationChoicePresenter.Builder(
                    RepositoryProvider.provideRepository(getApplicationContext()),
                    DefaultSchedulerProvider.getInstance())
                    .build();
            presenter.sendUiEvent(LoadLocationsEvent.INSTANCE);
        }

        toolbar.setTitle(R.string.location_choice_title);
        toolbar.setNavigationIcon(R.drawable.ic_menu_back);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        adapter = new ListAdapter(this, Collections.emptyList());
        adapter.setClickListener(position -> presenter.sendUiEvent(
                LocationClickEvent.create(position))
        );

        locationsView.setHasFixedSize(true);
        locationsView.setAdapter(adapter);
        locationsView.setLayoutManager(new LinearLayoutManager(this));
        locationsView.addItemDecoration(new DividerItemDecoration(this,
                LinearLayoutManager.VERTICAL));

        listHelper = new ListViewHelper(locationsView, emptyListView, loadingProgressView);
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
    public Object onRetainCustomNonConfigurationInstance() {
        return presenter;
    }

    private void acceptNewUiModel(LocationChoiceUiModel newModel) {
        if (newModel.isLoadingError()) {
            showError(R.string.error_loading_data);
            presenter.sendUiEvent(LoadLocationsErrorConfirmEvent.INSTANCE);
            return;
        }

        if (newModel.getLocationToOpen().isPresent()) {
            sendLocationAsResult(newModel.getLocationToOpen().getValue());
            finish();
            return;
        }

        adapter.changeDataSet(newModel.getLocations());

        if (newModel.isLoading()) {
            listHelper.showProgressView();
        } else if (newModel.getLocations().isEmpty()) {
            listHelper.showEmptyList();
        } else {
            listHelper.showList();
        }

        requestLayout();
    }

    private void sendLocationAsResult(Location location) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_LOCATION, new Gson().toJson(location));

        setResult(RESULT_OK, intent);
    }

    private void showError(int msg) {
        Snackbar.make(locationsView, msg, Snackbar.LENGTH_SHORT).show();
    }

    private void requestLayout() {
        ScreenUtils.postRequestLayout(rootView);
    }
}
