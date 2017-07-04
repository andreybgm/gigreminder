package io.github.andreybgm.gigreminder.utils;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.Set;

import io.reactivex.Observable;

public class ScreenUtils {
    private ScreenUtils() {
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm =
                (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getWindow().getDecorView();
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void showView(View view) {
        if (view.getVisibility() != View.VISIBLE) {
            view.setVisibility(View.VISIBLE);
        }
    }

    public static void hideView(View view) {
        if (view.getVisibility() != View.GONE) {
            view.setVisibility(View.GONE);
        }
    }

    public static void postRequestLayout(View view) {
        view.post(view::requestLayout);
    }

    public static void remainRightFab(@Nullable FloatingActionButton rightFab,
                                      @NonNull Set<FloatingActionButton> allFabs) {
        Observable<FloatingActionButton> fabsObservable = Observable.fromIterable(allFabs);

        if (rightFab == null) {
            fabsObservable
                    .filter(fab -> fab.getVisibility() != View.GONE)
                    .blockingForEach(FloatingActionButton::hide);
        } else if (rightFab.getVisibility() == View.VISIBLE) {
            fabsObservable
                    .filter(fab -> fab != rightFab)
                    .blockingForEach(FloatingActionButton::hide);
        } else {
            FloatingActionButton visibleFab = fabsObservable
                    .filter(fab -> fab.getVisibility() == View.VISIBLE)
                    .firstElement()
                    .blockingGet();
            fabsObservable
                    .filter(fab -> fab != rightFab && fab != visibleFab)
                    .blockingForEach(FloatingActionButton::hide);

            if (visibleFab != null) {
                visibleFab.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                    @Override
                    public void onHidden(FloatingActionButton fab) {
                        rightFab.show();
                    }
                });
            } else {
                rightFab.show();
            }

        }
    }

    public static void remainVisibleViews(Iterable<View> allViews, Set<View> visibleViews) {
        Observable.fromIterable(allViews)
                .filter(view -> !visibleViews.contains(view))
                .blockingForEach(ScreenUtils::hideView);
        Observable.fromIterable(visibleViews)
                .blockingForEach(ScreenUtils::showView);
    }
}
