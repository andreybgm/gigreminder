package io.github.andreybgm.gigreminder.screen.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Menu;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.andreybgm.gigreminder.R;
import io.github.andreybgm.gigreminder.repository.sync.SyncManager;
import io.github.andreybgm.gigreminder.screen.artists.ArtistsFragment;
import io.github.andreybgm.gigreminder.screen.concerts.ConcertsFragment;
import io.github.andreybgm.gigreminder.screen.locations.LocationsFragment;
import io.github.andreybgm.gigreminder.utils.ScreenUtils;
import io.github.andreybgm.gigreminder.utils.retainedstate.RetainedStateHolder;

public class MainActivity extends AppCompatActivity {

    private final RetainedStateHolder stateHolder;
    private Set<FloatingActionButton> allFabs;
    private ActionMode currentActionMode;

    @BindView(R.id.fab_add_artist)
    FloatingActionButton addArtistFab;

    @BindView(R.id.fab_add_location)
    FloatingActionButton addLocationFab;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    public static Intent makeIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        return intent;
    }

    public MainActivity() {
        super();
        stateHolder = RetainedStateHolder.create(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        List<PagerTabData> tabs = Arrays.asList(
                PagerTabData.create(getString(R.string.title_tab_concerts), ConcertsFragment::newInstance),
                PagerTabData.create(getString(R.string.title_tab_artists), ArtistsFragment::newInstance),
                PagerTabData.create(getString(R.string.title_tab_locations), LocationsFragment::newInstance)
        );
        MainPagerAdapter adapter = new MainPagerAdapter(getSupportFragmentManager(), tabs);

        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);
        pager.addOnPageChangeListener(createOnPageChangeListener());

        TabLayout tabsView = (TabLayout) findViewById(R.id.tabs);
        tabsView.setupWithViewPager(pager);
        tabsView.setTabMode(TabLayout.MODE_FIXED);

        allFabs = new HashSet<>();
        allFabs.add(addArtistFab);
        allFabs.add(addLocationFab);
        showFab(0);

        SyncManager.init(getApplicationContext());
    }

    @Override
    public void onSupportActionModeStarted(@NonNull ActionMode mode) {
        super.onSupportActionModeStarted(mode);
        currentActionMode = mode;
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return stateHolder.createHolderState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    public RetainedStateHolder getStateHolder() {
        return stateHolder;
    }

    @NonNull
    private ViewPager.OnPageChangeListener createOnPageChangeListener() {
        return new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (currentActionMode != null) {
                    currentActionMode.finish();
                    currentActionMode = null;
                }

                showFab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        };
    }

    private void showFab(int tabPosition) {
        SparseArray<FloatingActionButton> positionToFab = new SparseArray<>();
        positionToFab.put(1, addArtistFab);
        positionToFab.put(2, addLocationFab);

        ScreenUtils.remainRightFab(positionToFab.get(tabPosition, null), allFabs);
    }
}
