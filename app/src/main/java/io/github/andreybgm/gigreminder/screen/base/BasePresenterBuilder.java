package io.github.andreybgm.gigreminder.screen.base;

import android.support.annotation.NonNull;

import io.github.andreybgm.gigreminder.repository.DataSource;
import io.github.andreybgm.gigreminder.utils.Optional;
import io.github.andreybgm.gigreminder.utils.schedulers.SchedulerProvider;

public abstract class BasePresenterBuilder<
        BuilderT extends BasePresenterBuilder<BuilderT, PresenterT, UiModelT>,
        PresenterT extends BasePresenter,
        UiModelT extends UiModel> {

    @NonNull
    private final DataSource repository;
    @NonNull
    private final SchedulerProvider schedulerProvider;

    @NonNull
    private Optional<UiModelT> uiModel;

    public BasePresenterBuilder(@NonNull DataSource repository,
                                @NonNull SchedulerProvider schedulerProvider) {
        this.repository = repository;
        this.schedulerProvider = schedulerProvider;
        this.uiModel = Optional.empty();
    }

    @NonNull
    public abstract PresenterT build();

    @NonNull
    public abstract BuilderT getThis();

    public BuilderT uiModel(@NonNull UiModelT uiModel) {
        this.uiModel = Optional.of(uiModel);
        return getThis();
    }

    @NonNull
    public DataSource getRepository() {
        return repository;
    }

    @NonNull
    public SchedulerProvider getSchedulerProvider() {
        return schedulerProvider;
    }

    @NonNull
    public Optional<UiModelT> getUiModel() {
        return uiModel;
    }
}
