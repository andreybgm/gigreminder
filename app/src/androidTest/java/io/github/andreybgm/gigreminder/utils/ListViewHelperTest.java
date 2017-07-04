package io.github.andreybgm.gigreminder.utils;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.view.View;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ListViewHelperTest {

    private ListViewHelper listHelper;
    private View listView;
    private View emptyListView;
    private View progressView;

    @Before
    public void setUp() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();
        listView = new View(context);
        emptyListView = new View(context);
        progressView = new View(context);

        listHelper = new ListViewHelper(listView, emptyListView, progressView);
    }

    @Test
    public void showList() throws Exception {
        listHelper.showList();

        assertThat(listView.getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(emptyListView.getVisibility()).isEqualTo(View.GONE);
        assertThat(progressView.getVisibility()).isEqualTo(View.GONE);
    }


    @Test
    public void showEmptyList() throws Exception {
        listHelper.showEmptyList();

        assertThat(listView.getVisibility()).isEqualTo(View.GONE);
        assertThat(emptyListView.getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(progressView.getVisibility()).isEqualTo(View.GONE);
    }

    @Test
    public void showProgressView() throws Exception {
        listHelper.showProgressView();

        assertThat(listView.getVisibility()).isEqualTo(View.GONE);
        assertThat(emptyListView.getVisibility()).isEqualTo(View.GONE);
        assertThat(progressView.getVisibility()).isEqualTo(View.VISIBLE);
    }

    @Test
    public void hideProgressView() throws Exception {
        listHelper.hideProgressView();

        assertThat(progressView.getVisibility()).isEqualTo(View.GONE);
    }
}