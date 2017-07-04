package io.github.andreybgm.gigreminder.screen.artists;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.andreybgm.gigreminder.R;
import io.github.andreybgm.gigreminder.data.Artist;
import io.github.andreybgm.gigreminder.repository.RepositoryProvider;
import io.github.andreybgm.gigreminder.screen.artistimport.ArtistImportActivity;
import io.github.andreybgm.gigreminder.screen.artists.uievent.ArtistClickEvent;
import io.github.andreybgm.gigreminder.screen.artists.uievent.ArtistLongClickEvent;
import io.github.andreybgm.gigreminder.screen.artists.uievent.DeleteArtistsEvent;
import io.github.andreybgm.gigreminder.screen.artists.uievent.LoadArtistsErrorConfirmEvent;
import io.github.andreybgm.gigreminder.screen.artists.uievent.LoadArtistsEvent;
import io.github.andreybgm.gigreminder.screen.artists.uievent.OpenArtistConfirmEvent;
import io.github.andreybgm.gigreminder.screen.artists.uievent.OpenArtistsImportEvent;
import io.github.andreybgm.gigreminder.screen.artists.uievent.OpenNewArtistEvent;
import io.github.andreybgm.gigreminder.screen.artists.uievent.TurnOffActionModeEvent;
import io.github.andreybgm.gigreminder.screen.base.RxUtils;
import io.github.andreybgm.gigreminder.screen.editartist.EditArtistActivity;
import io.github.andreybgm.gigreminder.screen.main.MainActivity;
import io.github.andreybgm.gigreminder.utils.ListViewHelper;
import io.github.andreybgm.gigreminder.utils.ScreenUtils;
import io.github.andreybgm.gigreminder.utils.retainedstate.RetainedStateHolder;
import io.github.andreybgm.gigreminder.utils.retainedstate.RetainedStateUtils;
import io.github.andreybgm.gigreminder.utils.schedulers.DefaultSchedulerProvider;
import io.reactivex.disposables.CompositeDisposable;

public class ArtistsFragment extends Fragment {

    private static final String STATE_ACTION_MODE_ON = "ACTION_MODE_ON";
    private static final String STATE_SELECTED_ARTIST_IDS = "SELECTED_ARTIST_IDS";

    @BindView(R.id.list_names)
    RecyclerView artistsView;

    @BindView(R.id.empty_list)
    View emptyListView;

    @BindView(R.id.progress_loading)
    View loadingProgressView;

    private ArtistsPresenter presenter;
    private int retainedStateId;
    private ListAdapter adapter;
    private ActionMode actionMode;
    private ListViewHelper listHelper;
    private View rootView;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    @NonNull
    private ArtistsUiModel uiModel = ArtistsUiModel.DEFAULT;

    public static ArtistsFragment newInstance() {
        return new ArtistsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        RetainedStateHolder stateHolder = ((MainActivity) getActivity()).getStateHolder();
        retainedStateId = stateHolder.addProvider(
                RetainedStateUtils.restoreStateId(savedInstanceState),
                () -> presenter);
        presenter = stateHolder.restoreState(retainedStateId);

        if (presenter == null) {
            presenter = createPresenter(savedInstanceState);
            presenter.sendUiEvent(LoadArtistsEvent.INSTANCE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_artists, container, false);
        ButterKnife.bind(this, rootView);

        adapter = new ListAdapter(getContext(), uiModel);
        adapter.setOnItemClickListener(createOnArtistClickListener());

        artistsView.setHasFixedSize(true);
        artistsView.setAdapter(adapter);
        artistsView.setLayoutManager(new LinearLayoutManager(getContext()));
        artistsView.addItemDecoration(new DividerItemDecoration(getContext(),
                LinearLayoutManager.VERTICAL));

        listHelper = new ListViewHelper(artistsView, emptyListView, loadingProgressView);

        FloatingActionButton addArtistButton = (FloatingActionButton) getActivity().findViewById(
                R.id.fab_add_artist);
        addArtistButton.setOnClickListener(v -> {
            presenter.sendUiEvent(OpenNewArtistEvent.INSTANCE);
        });

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

        List<String> selectedArtistIds = uiModel.getSelectedArtistIds();
        String[] ids = selectedArtistIds.toArray(new String[selectedArtistIds.size()]);
        outState.putStringArray(STATE_SELECTED_ARTIST_IDS, ids);

        outState.putBoolean(STATE_ACTION_MODE_ON, uiModel.isActionModeOn());

        RetainedStateUtils.putStateIdToBundle(outState, retainedStateId);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_artists, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_import_artists: {
                presenter.sendUiEvent(OpenArtistsImportEvent.INSTANCE);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @NonNull
    private ArtistsPresenter createPresenter(Bundle savedInstanceState) {

        boolean actionModeOn;
        List<String> selectedArtistIds;

        if (savedInstanceState != null) {
            actionModeOn = savedInstanceState.getBoolean(STATE_ACTION_MODE_ON);
            //noinspection ConstantConditions
            selectedArtistIds = Arrays.asList(
                    savedInstanceState.getStringArray(STATE_SELECTED_ARTIST_IDS));
        } else {
            actionModeOn = false;
            selectedArtistIds = Collections.emptyList();
        }

        return new ArtistsPresenter.Builder(
                RepositoryProvider.provideRepository(getActivity().getApplicationContext()),
                DefaultSchedulerProvider.getInstance())
                .actionMode(actionModeOn, selectedArtistIds)
                .build();
    }

    private void acceptNewUiModel(ArtistsUiModel newModel) {
        if (newModel.getArtistToOpen().isPresent()) {
            Artist artist = newModel.getArtistToOpen().getValue();
            startActivity(EditArtistActivity.makeEditArtistIntent(getContext(), artist.getId()));
            presenter.sendUiEvent(OpenArtistConfirmEvent.INSTANCE);
            return;
        }

        if (newModel.isShouldOpenNewArtist()) {
            startActivity(EditArtistActivity.makeNewArtistIntent(getContext()));
            presenter.sendUiEvent(OpenNewArtistEvent.CONFIRMATION);
            return;
        }

        if (newModel.isShouldOpenArtistsImport()) {
            startActivity(ArtistImportActivity.makeIntent(getContext()));
            presenter.sendUiEvent(OpenArtistsImportEvent.CONFIRMATION);
            return;
        }

        if (newModel.isLoadingError()) {
            showError(R.string.error_loading_data);
            presenter.sendUiEvent(LoadArtistsErrorConfirmEvent.INSTANCE);
            return;
        }

        ArtistsUiModel currentModel = this.uiModel;
        this.uiModel = newModel;

        adapter.changeDataSet(newModel);

        if (newModel.isLoading()) {
            listHelper.showProgressView();
        } else if (newModel.getArtists().isEmpty()) {
            listHelper.showEmptyList();
        } else {
            listHelper.showList();
        }

        if (newModel.isActionModeOn() != currentModel.isActionModeOn()) {
            if (newModel.isActionModeOn()) {
                actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(
                        createActionModeCallback());
            } else if (actionMode != null) {
                actionMode.finish();
            }
        }

        if (newModel.isActionModeOn() && actionMode != null) {
            int selectedCount = uiModel.getSelectedArtists().size();
            actionMode.setTitle(String.valueOf(selectedCount));
        }

        requestLayout();
    }

    private void showError(int msg) {
        Snackbar.make(rootView, msg, Snackbar.LENGTH_LONG).show();
    }

    @NonNull
    private ListAdapter.OnItemClickListener createOnArtistClickListener() {
        return new ListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Artist artist, int position) {
                presenter.sendUiEvent(ArtistClickEvent.create(position));
            }

            @Override
            public void onItemLongClick(Artist artist, int position) {
                presenter.sendUiEvent(ArtistLongClickEvent.create(position));
            }
        };
    }

    @NonNull
    private ActionMode.Callback createActionModeCallback() {
        return new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                getActivity().getMenuInflater().inflate(
                        R.menu.fragment_artists_action_mode, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete: {
                        presenter.sendUiEvent(DeleteArtistsEvent.create(uiModel));
                        return true;
                    }
                }

                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                actionMode = null;
                presenter.sendUiEvent(TurnOffActionModeEvent.INSTANCE);
            }
        };
    }

    private void requestLayout() {
        ScreenUtils.postRequestLayout(rootView);
    }
}
