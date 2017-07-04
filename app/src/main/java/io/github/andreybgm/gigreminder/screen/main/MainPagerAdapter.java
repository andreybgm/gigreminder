package io.github.andreybgm.gigreminder.screen.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

class MainPagerAdapter extends FragmentPagerAdapter {

    private final List<PagerTabData> tabs;

    MainPagerAdapter(FragmentManager fm, List<PagerTabData> tabs) {
        super(fm);
        this.tabs = tabs;
    }

    @Override
    public Fragment getItem(int position) {
        try {
            PagerTabData tabData = tabs.get(position);
            return tabData.fragmentConstructor.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getCount() {
        return tabs.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position > tabs.size() - 1) {
            return "";
        }

        return tabs.get(position).title;
    }
}
