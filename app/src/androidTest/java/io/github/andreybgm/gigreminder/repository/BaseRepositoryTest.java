package io.github.andreybgm.gigreminder.repository;

import org.junit.After;
import org.junit.Before;

import io.github.andreybgm.gigreminder.repository.db.DbHelper;
import io.github.andreybgm.gigreminder.repository.sync.SyncManager;
import io.github.andreybgm.gigreminder.test.TestUtils;

class BaseRepositoryTest {
    @Before
    public void setUp() throws Exception {
        cleanUp();
        SyncManager.disableSync(TestUtils.getContext());
    }

    @After
    public void tearDown() throws Exception {
        cleanUp();
    }

    private void cleanUp() {
        RepositoryProvider.reset();
        TestUtils.getContext().deleteDatabase(DbHelper.DATABASE_NAME);
    }

}
