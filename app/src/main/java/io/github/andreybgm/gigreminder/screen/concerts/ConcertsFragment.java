package io.github.andreybgm.gigreminder.screen.concerts;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.andreybgm.gigreminder.R;
import io.github.andreybgm.gigreminder.data.Concert;
import io.github.andreybgm.gigreminder.repository.RepositoryProvider;
import io.github.andreybgm.gigreminder.screen.base.RxUtils;
import io.github.andreybgm.gigreminder.screen.concertdetails.ConcertDetailsActivity;
import io.github.andreybgm.gigreminder.screen.concerts.uievent.ConcertClickEvent;
import io.github.andreybgm.gigreminder.screen.concerts.uievent.LoadConcertsErrorConfirmEvent;
import io.github.andreybgm.gigreminder.screen.concerts.uievent.LoadConcertsEvent;
import io.github.andreybgm.gigreminder.screen.concerts.uievent.OpenConcertConfirmEvent;
import io.github.andreybgm.gigreminder.screen.main.MainActivity;
import io.github.andreybgm.gigreminder.utils.ScreenUtils;
import io.github.andreybgm.gigreminder.utils.retainedstate.RetainedStateHolder;
import io.github.andreybgm.gigreminder.utils.retainedstate.RetainedStateUtils;
import io.github.andreybgm.gigreminder.utils.schedulers.DefaultSchedulerProvider;
import io.reactivex.disposables.CompositeDisposable;

public class ConcertsFragment extends Fragment {
    @BindView(R.id.list_concerts)
    RecyclerView concertsView;

    @BindView(R.id.empty_list)
    View emptyListView;

    @BindView(R.id.progress_loading)
    View loadingProgressView;

    @BindView(R.id.progress_sync_small)
    View syncSmallProgressView;

    @BindView(R.id.progress_sync_big)
    View syncBigProgressView;

    private ConcertsPresenter presenter;
    private int retainedStateId;
    private ListAdapter adapter;
    private View rootView;
    private Set<View> allViews;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public static ConcertsFragment newInstance() {
        return new ConcertsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RetainedStateHolder stateHolder = ((MainActivity) getActivity()).getStateHolder();
        retainedStateId = stateHolder.addProvider(
                RetainedStateUtils.restoreStateId(savedInstanceState), () -> presenter);
        presenter = stateHolder.restoreState(retainedStateId);

        if (presenter == null) {
            presenter = new ConcertsPresenter.Builder(
                    RepositoryProvider.provideRepository(getActivity().getApplicationContext()),
                    DefaultSchedulerProvider.getInstance())
                    .currentTimeProvider(Date::new)
                    .build();
            presenter.sendUiEvent(LoadConcertsEvent.INSTANCE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_concerts, container, false);
        ButterKnife.bind(this, rootView);

        adapter = new ListAdapter(getContext(), Collections.emptyList());
        adapter.setItemClickListener(position ->
                presenter.sendUiEvent(ConcertClickEvent.create(position))
        );

        concertsView.setHasFixedSize(true);
        concertsView.setAdapter(adapter);
        concertsView.setLayoutManager(new LinearLayoutManager(getContext()));
        concertsView.addItemDecoration(new DividerItemDecoration(getContext(),
                LinearLayoutManager.VERTICAL));

        allViews = new HashSet<>();
        allViews.add(concertsView);
        allViews.add(emptyListView);
        allViews.add(loadingProgressView);
        allViews.add(syncSmallProgressView);
        allViews.add(syncBigProgressView);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        compositeDisposable.add(
                presenter.getUiModels()
                        .compose(RxUtils.observeOnUiWithDebounce())
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

    private void acceptNewUiModel(ConcertsUiModel newModel) {
        if (newModel.getConcertToOpen().isPresent()) {
            openConcertDetails(newModel.getConcertToOpen().getValue());
            presenter.sendUiEvent(OpenConcertConfirmEvent.INSTANCE);
            return;
        }

        if (newModel.isLoadingError()) {
            showError(R.string.error_loading_data);
            presenter.sendUiEvent(LoadConcertsErrorConfirmEvent.INSTANCE);
            return;
        }

        adapter.changeDataSet(newModel.getConcerts());

        Set<View> visibleViews = new HashSet<>();

        if (newModel.isSyncing() && newModel.isLoading()) {
            visibleViews.add(syncBigProgressView);
        } else if (newModel.isSyncing() && newModel.getConcerts().isEmpty()) {
            visibleViews.add(syncBigProgressView);
        } else if (newModel.isSyncing() && !newModel.getConcerts().isEmpty()) {
            visibleViews.add(syncSmallProgressView);
            visibleViews.add(concertsView);
        } else if (newModel.isLoading()) {
            visibleViews.add(loadingProgressView);
        } else if (!newModel.getConcerts().isEmpty()) {
            visibleViews.add(concertsView);
        } else {
            visibleViews.add(emptyListView);
        }

        ScreenUtils.remainVisibleViews(allViews, visibleViews);
        requestLayout();
    }

    private void openConcertDetails(@NonNull Concert concert) {
        Intent intent = ConcertDetailsActivity.makeIntent(getContext(), concert);
        startActivity(intent);
    }

    private void showError(int msg) {
        Snackbar.make(rootView, msg, Snackbar.LENGTH_SHORT).show();
    }

    private void requestLayout() {
        ScreenUtils.postRequestLayout(rootView);
    }
}
