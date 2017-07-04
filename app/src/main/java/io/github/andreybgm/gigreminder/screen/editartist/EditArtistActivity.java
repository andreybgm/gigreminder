package io.github.andreybgm.gigreminder.screen.editartist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.andreybgm.gigreminder.R;
import io.github.andreybgm.gigreminder.data.Artist;
import io.github.andreybgm.gigreminder.repository.RepositoryProvider;
import io.github.andreybgm.gigreminder.screen.base.RxUtils;
import io.github.andreybgm.gigreminder.screen.dialog.DiscardDialog;
import io.github.andreybgm.gigreminder.screen.editartist.uievent.DiscardConfirmEvent;
import io.github.andreybgm.gigreminder.screen.editartist.uievent.DiscardEvent;
import io.github.andreybgm.gigreminder.screen.editartist.uievent.LoadArtistEvent;
import io.github.andreybgm.gigreminder.screen.editartist.uievent.SaveArtistErrorConfirmEvent;
import io.github.andreybgm.gigreminder.screen.editartist.uievent.SaveArtistEvent;
import io.github.andreybgm.gigreminder.screen.editartist.uievent.ViewDataIsFilledEvent;
import io.github.andreybgm.gigreminder.utils.ScreenUtils;
import io.github.andreybgm.gigreminder.utils.schedulers.DefaultSchedulerProvider;
import io.reactivex.disposables.CompositeDisposable;

public class EditArtistActivity extends AppCompatActivity implements
        Toolbar.OnMenuItemClickListener, DiscardDialog.DiscardDialogListener {

    private static final String EXTRA_ARTIST_ID = "ARTIST_ID";
    private static final String STATE_IS_VIEW_DATA_FILLED = "IS_VIEW_DATA_FILLED";
    private static final String TAG_DISCARD_DIALOG = "DISCARD_DIALOG";

    @BindView(R.id.screen_edit_artist)
    View rootView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.input_layout_name)
    TextInputLayout inputLayoutName;

    @BindView(R.id.edit_name)
    EditText editTextName;

    private EditArtistPresenter presenter;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    @NonNull
    private EditArtistUiModel uiModel = EditArtistUiModel.NEW_ARTIST;

    public static Intent makeNewArtistIntent(Context context) {
        return new Intent(context, EditArtistActivity.class);
    }

    public static Intent makeEditArtistIntent(Context context, String id) {
        Intent intent = new Intent(context, EditArtistActivity.class);
        intent.putExtra(EXTRA_ARTIST_ID, id);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_artist);
        ButterKnife.bind(this);

        inputLayoutName.setHintAnimationEnabled(false);

        presenter = (EditArtistPresenter) getLastCustomNonConfigurationInstance();

        if (presenter == null) {
            EditArtistPresenter.Builder builder = new EditArtistPresenter.Builder(
                    RepositoryProvider.provideRepository(getApplicationContext()),
                    DefaultSchedulerProvider.getInstance());
            Intent intent = getIntent();
            String artistId = intent.getStringExtra(EXTRA_ARTIST_ID);

            if (artistId != null) {
                boolean viewDataIsFilled = savedInstanceState != null
                        && savedInstanceState.getBoolean(STATE_IS_VIEW_DATA_FILLED);
                presenter = builder
                        .artistId(artistId)
                        .viewDataIsFilled(viewDataIsFilled)
                        .build();
                presenter.sendUiEvent(LoadArtistEvent.INSTANCE);
            } else {
                presenter = builder.build();
            }
        }

        toolbar.setNavigationIcon(R.drawable.ic_menu_discard);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.inflateMenu(R.menu.activity_editartist);
        toolbar.setOnMenuItemClickListener(this);
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_IS_VIEW_DATA_FILLED, uiModel.isViewDataIsFilled());
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return presenter;
    }

    @Override
    public void onBackPressed() {
        presenter.sendUiEvent(DiscardEvent.create(getCurrentName(), false));
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save: {
                ScreenUtils.hideKeyboard(this);
                presenter.sendUiEvent(SaveArtistEvent.create(uiModel, getCurrentName()));

                return true;
            }
        }

        return false;
    }

    @Override
    public void onDiscardDialogConfirmClick() {
        presenter.sendUiEvent(DiscardEvent.create(getCurrentName(), true));
    }

    private void acceptUiModel(EditArtistUiModel newModel) {
        if (newModel.isLoadingError()) {
            Toast.makeText(this, R.string.error_loading_data, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (newModel.isShouldClose()) {
            finish();
            return;
        }

        if (newModel.isSavingError()) {
            showError(R.string.error_saving_data);
            presenter.sendUiEvent(SaveArtistErrorConfirmEvent.INSTANCE);
            return;
        }

        if (newModel.isShouldAskToDiscard()) {
            showDiscardDialog(newModel.getDiscardMsg().getValue());
            presenter.sendUiEvent(DiscardConfirmEvent.INSTANCE);
            return;
        }

        boolean shouldFillViewData = !newModel.isArtistNew()
                && !newModel.isLoading()
                && !newModel.isViewDataIsFilled();

        if (shouldFillViewData) {
            fillArtistData(newModel.getInitialArtist().getValue());
            presenter.sendUiEvent(ViewDataIsFilledEvent.INSTANCE);
            return;
        }

        uiModel = newModel;
        toolbar.setTitle(newModel.isArtistNew() ? R.string.new_artist : R.string.existing_artist);

        if (newModel.isFillError() && newModel.isEmptyNameError()) {
            showEmptyNameError();
        } else if (newModel.isFillError() && newModel.isNotUniqueNameError()) {
            showArtistNameNotUniqueError();
        } else {
            hideNameError();
        }
    }

    private String getCurrentName() {
        return editTextName.getText().toString();
    }

    private void fillArtistData(@NonNull Artist artist) {
        editTextName.setText(artist.getName());
        inputLayoutName.setHintAnimationEnabled(true);
    }

    private void showError(int msg) {
        Snackbar.make(rootView, msg, Snackbar.LENGTH_LONG).show();
    }

    private void showEmptyNameError() {
        showNameFillError(R.string.error_artist_name_empty);
    }

    private void showArtistNameNotUniqueError() {
        showNameFillError(R.string.error_artist_name_not_unique);
    }

    private void showNameFillError(int msg) {
        inputLayoutName.setErrorEnabled(true);
        inputLayoutName.setError(getString(msg));
    }

    private void hideNameError() {
        inputLayoutName.setErrorEnabled(false);
    }

    private void showDiscardDialog(int msg) {
        DiscardDialog.newInstance(msg)
                .show(getSupportFragmentManager(), TAG_DISCARD_DIALOG);
    }
}
