package io.github.andreybgm.gigreminder.screen.main;

import android.support.v4.app.Fragment;

import java.util.concurrent.Callable;

class PagerTabData {
    final String title;
    final Callable<Fragment> fragmentConstructor;

    static PagerTabData create(String title, Callable<Fragment> fragmentConstructor) {
        return new PagerTabData(title, fragmentConstructor);
    }

    private PagerTabData(String title, Callable<Fragment> fragmentConstructor) {
        this.title = title;
        this.fragmentConstructor = fragmentConstructor;
    }
}
