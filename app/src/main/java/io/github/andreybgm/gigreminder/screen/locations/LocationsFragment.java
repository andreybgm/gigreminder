package io.github.andreybgm.gigreminder.screen.locations;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.andreybgm.gigreminder.R;
import io.github.andreybgm.gigreminder.data.Location;
import io.github.andreybgm.gigreminder.repository.RepositoryProvider;
import io.github.andreybgm.gigreminder.screen.base.RxUtils;
import io.github.andreybgm.gigreminder.screen.locationchoice.LocationChoiceActivity;
import io.github.andreybgm.gigreminder.screen.locations.uievent.DeleteLocationErrorConfirmEvent;
import io.github.andreybgm.gigreminder.screen.locations.uievent.LoadLocationsErrorConfirmEvent;
import io.github.andreybgm.gigreminder.screen.locations.uievent.LoadLocationsEvent;
import io.github.andreybgm.gigreminder.screen.locations.uievent.OpenNewLocationEvent;
import io.github.andreybgm.gigreminder.screen.locations.uievent.SaveLocationErrorConfirmEvent;
import io.github.andreybgm.gigreminder.screen.locations.uievent.SaveLocationEvent;
import io.github.andreybgm.gigreminder.screen.main.MainActivity;
import io.github.andreybgm.gigreminder.utils.ListViewHelper;
import io.github.andreybgm.gigreminder.utils.ScreenUtils;
import io.github.andreybgm.gigreminder.utils.retainedstate.RetainedStateHolder;
import io.github.andreybgm.gigreminder.utils.retainedstate.RetainedStateUtils;
import io.github.andreybgm.gigreminder.utils.schedulers.DefaultSchedulerProvider;
import io.reactivex.disposables.CompositeDisposable;

import static android.app.Activity.RESULT_OK;

public class LocationsFragment extends Fragment {

    private static final int REQUEST_LOCATION_CHOICE = 1;

    @BindView(R.id.list_locations)
    RecyclerView locationsView;

    @BindView(R.id.empty_list)
    View emptyListView;

    @BindView(R.id.progress_loading)
    View loadingProgressView;

    private LocationsPresenter presenter;
    private int retainedStateId;
    private ListAdapter adapter;
    private ListViewHelper listHelper;
    private View rootView;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public static LocationsFragment newInstance() {
        return new LocationsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RetainedStateHolder stateHolder = ((MainActivity) getActivity()).getStateHolder();
        retainedStateId = stateHolder.addProvider(
                RetainedStateUtils.restoreStateId(savedInstanceState), () -> presenter);
        presenter = stateHolder.restoreState(retainedStateId);

        if (presenter == null) {
            presenter = new LocationsPresenter.Builder(
                    RepositoryProvider.provideRepository(getActivity().getApplicationContext()),
                    DefaultSchedulerProvider.getInstance())
                    .build();
            presenter.sendUiEvent(LoadLocationsEvent.INSTANCE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_locations, container, false);
        ButterKnife.bind(this, rootView);

        adapter = new ListAdapter(getContext(), LocationsUiModel.DEFAULT, presenter);

        locationsView.setHasFixedSize(true);
        locationsView.setLayoutManager(new LinearLayoutManager(getContext()));
        locationsView.setAdapter(adapter);
        locationsView.addItemDecoration(new DividerItemDecoration(getContext(),
                LinearLayoutManager.VERTICAL));

        View addLocationButton = getActivity().findViewById(R.id.fab_add_location);
        addLocationButton.setOnClickListener(v ->
                presenter.sendUiEvent(OpenNewLocationEvent.INSTANCE));

        listHelper = new ListViewHelper(locationsView, emptyListView, loadingProgressView);

        return rootView;
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        RetainedStateUtils.putStateIdToBundle(outState, retainedStateId);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOCATION_CHOICE && resultCode == RESULT_OK) {
            String extra = data.getStringExtra(LocationChoiceActivity.EXTRA_LOCATION);
            Location location = new Gson().fromJson(extra, Location.class);
            presenter.sendUiEvent(SaveLocationEvent.create(location));
        }
    }

    private void acceptNewUiModel(LocationsUiModel newModel) {
        if (newModel.isLoadingError()) {
            showError(R.string.error_loading_data);
            presenter.sendUiEvent(LoadLocationsErrorConfirmEvent.INSTANCE);
            return;
        }

        if (newModel.isDeletionError()) {
            showError(R.string.error_deletion_data);
            presenter.sendUiEvent(DeleteLocationErrorConfirmEvent.INSTANCE);
            return;
        }

        if (newModel.isSavingError()) {
            showError(R.string.error_deletion_data);
            presenter.sendUiEvent(SaveLocationErrorConfirmEvent.INSTANCE);
            return;
        }

        if (newModel.isShouldOpenNewLocation()) {
            startActivityForResult(LocationChoiceActivity.makeIntent(getContext()),
                    REQUEST_LOCATION_CHOICE);
            presenter.sendUiEvent(OpenNewLocationEvent.CONFIRMATION);
            return;
        }

        adapter.changeDataSet(newModel);

        if (newModel.isLoading()) {
            listHelper.showProgressView();
        } else if (newModel.getLocations().isEmpty()) {
            listHelper.showEmptyList();
        } else {
            listHelper.showList();
        }

        requestLayout();
    }

    private void showError(int msg) {
        Snackbar.make(rootView, msg, Snackbar.LENGTH_LONG).show();
    }

    private void requestLayout() {
        ScreenUtils.postRequestLayout(rootView);
    }
}
