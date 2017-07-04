package io.github.andreybgm.gigreminder.utils.retainedstate;

import android.support.v7.app.AppCompatActivity;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RetainedStateHolderTest {
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Test
    public void addProvider() throws Exception {
        RetainedStateHolder retainedStateHolder = new RetainedStateHolder();
        int id = retainedStateHolder.addProvider(() -> null);

        assertThat(id).isNotEqualTo(RetainedStateHolder.EMPTY_ID);
    }

    @Test
    public void createHolderState() throws Exception {
        RetainedStateHolder retainedStateHolder = new RetainedStateHolder();
        retainedStateHolder.addProvider(() -> "State");

        Object holderState = retainedStateHolder.createHolderState();

        assertThat(holderState).isNotNull();
    }

    @Test
    public void restoreState() throws Exception {
        String initialState = "InitialState";
        RetainedStateHolder stateHolder = new RetainedStateHolder();
        int initialId = stateHolder.addProvider(() -> initialState);
        Object holderState = stateHolder.createHolderState();

        RetainedStateHolder restoredStateHolder = new RetainedStateHolder(holderState);
        int restoredId = restoredStateHolder.addProvider(initialId, () -> "AnotherState");
        String restoredState = restoredStateHolder.restoreState(restoredId);

        assertThat(restoredId).isEqualTo(initialId);
        assertThat(restoredState).isEqualTo(initialState);
    }

    @Test
    public void restoreFewStates() throws Exception {
        String initialState1 = "InitialState";
        int initialState2 = 10;
        Object initialState3 = new Object();
        RetainedStateHolder stateHolder = new RetainedStateHolder();
        int initialId1 = stateHolder.addProvider(() -> initialState1);
        int initialId2 = stateHolder.addProvider(() -> initialState2);
        int initialId3 = stateHolder.addProvider(() -> initialState3);
        Object holderState = stateHolder.createHolderState();

        RetainedStateHolder restoredStateHolder = new RetainedStateHolder(holderState);
        int restoredId1 = restoredStateHolder.addProvider(initialId1, () -> "AnotherState");
        int restoredId2 = restoredStateHolder.addProvider(initialId2, () -> 20);
        int restoredId3 = restoredStateHolder.addProvider(initialId3, Object::new);
        String restoredState1 = restoredStateHolder.restoreState(restoredId1);
        int restoredState2 = restoredStateHolder.restoreState(restoredId2);
        Object restoredState3 = restoredStateHolder.restoreState(restoredId3);

        assertThat(restoredId1).isEqualTo(initialId1);
        assertThat(restoredState1).isEqualTo(initialState1);

        assertThat(restoredId2).isEqualTo(initialId2);
        assertThat(restoredState2).isEqualTo(initialState2);

        assertThat(restoredId3).isEqualTo(initialId3);
        assertThat(restoredState3).isEqualTo(initialState3);
    }

    @Test
    public void createFromActivityWhenNoState() throws Exception {
        AppCompatActivity activity = mock(AppCompatActivity.class);
        when(activity.getLastCustomNonConfigurationInstance()).thenReturn(null);

        RetainedStateHolder stateHolder = RetainedStateHolder.create(activity);

        assertThat(stateHolder).isNotNull();
    }

    @Test
    public void createFromActivityWhenStateExists() throws Exception {
        String initialState = "InitialState";
        AppCompatActivity initialActivity = mock(AppCompatActivity.class);
        when(initialActivity.getLastCustomNonConfigurationInstance()).thenReturn(null);
        RetainedStateHolder initialStateHolder = RetainedStateHolder.create(initialActivity);
        int id = initialStateHolder.addProvider(() -> initialState);
        Object holderState = initialStateHolder.createHolderState();

        AppCompatActivity newActivity = mock(AppCompatActivity.class);
        when(newActivity.getLastCustomNonConfigurationInstance()).thenReturn(holderState);
        RetainedStateHolder newStateHolder = RetainedStateHolder.create(newActivity);
        String restoredState = newStateHolder.restoreState(id);

        assertThat(restoredState).isEqualTo(initialState);
    }
}