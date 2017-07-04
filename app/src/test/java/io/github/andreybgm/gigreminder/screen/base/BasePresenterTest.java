package io.github.andreybgm.gigreminder.screen.base;

import android.support.annotation.NonNull;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import io.github.andreybgm.gigreminder.repository.DataSource;
import io.github.andreybgm.gigreminder.utils.schedulers.ImmediateSchedulerProvider;
import io.github.andreybgm.gigreminder.utils.schedulers.SchedulerProvider;
import io.reactivex.Observable;

public class BasePresenterTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private DataSource repository;

    private TestPresenter presenter;
    private TestUiEvent uiEvent;
    private TestUiModel uiModel;

    @Before
    public void setUp() throws Exception {
        presenter = new TestPresenter.Builder(repository, new ImmediateSchedulerProvider())
                .build();
        uiEvent = new TestUiEvent(1);
        uiModel = new TestUiModel(1);
    }

    @Test
    public void shouldReturnDefaultUiModelImmediately() throws Exception {
        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertNotComplete()
                .assertValue(TestUiModel.DEFAULT);
    }

    @Test
    public void shouldCacheLastUiModel() throws Exception {
        presenter.sendUiEvent(uiEvent);

        presenter.getUiModels()
                .test()
                .awaitCount(1)
                .assertNoErrors()
                .assertValue(uiModel);
    }

    private static class TestPresenter extends BasePresenter<TestUiModel> {
        TestPresenter(@NonNull Builder builder) {
            super(builder);

            connect();
        }

        @Override
        protected TestUiModel getInitialUiModel() {
            return TestUiModel.DEFAULT;
        }

        @Override
        protected Observable<Result> handleEvents(Observable<UiEvent> events) {
            return events.map(event -> new TestResult(((TestUiEvent) event).data));
        }

        @Override
        protected TestUiModel reduceModel(TestUiModel model, Result result) {
            return new TestUiModel(((TestResult) result).data);
        }

        static class Builder extends BasePresenterBuilder<Builder, TestPresenter, TestUiModel> {
            public Builder(DataSource repository, SchedulerProvider schedulerProvider) {
                super(repository, schedulerProvider);
            }

            @Override
            @NonNull
            public TestPresenter build() {
                return new TestPresenter(this);
            }

            @NonNull
            @Override
            public Builder getThis() {
                return this;
            }
        }
    }

    private static class TestUiEvent implements UiEvent {
        private final int data;

        TestUiEvent(int data) {
            this.data = data;
        }
    }

    private static class TestResult implements Result {
        private final int data;

        TestResult(int data) {
            this.data = data;
        }
    }

    private static class TestUiModel implements UiModel {
        static final TestUiModel DEFAULT = new TestUiModel(0);

        private final int data;

        TestUiModel(int data) {
            this.data = data;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestUiModel that = (TestUiModel) o;

            return data == that.data;

        }

        @Override
        public int hashCode() {
            return data;
        }

        @Override
        public String toString() {
            return "TestUiModel{" +
                    "data=" + data +
                    '}';
        }
    }
}