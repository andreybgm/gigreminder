package io.github.andreybgm.gigreminder.utils;

import android.support.annotation.NonNull;
import android.view.View;

import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;

public class ListViewHelper {
    @NonNull
    private final View listView;
    @NonNull
    private final View emptyListView;
    @NonNull
    private final View progressView;
    private final List<View> allViews;

    public ListViewHelper(@NonNull View listView, @NonNull View emptyListView,
                          @NonNull View progressView) {
        this.listView = listView;
        this.emptyListView = emptyListView;
        this.progressView = progressView;
        allViews = Arrays.asList(listView, emptyListView, progressView);
    }

    public void showList() {
        showView(listView);
    }

    public void showEmptyList() {
        showView(emptyListView);
    }

    public void showProgressView() {
        showView(progressView);
    }

    public void hideProgressView() {
        ScreenUtils.hideView(progressView);
    }

    private void showView(View rightView) {
        Observable.fromIterable(allViews)
                .filter(view -> view != rightView)
                .blockingForEach(ScreenUtils::hideView);
        ScreenUtils.showView(rightView);
    }
}
