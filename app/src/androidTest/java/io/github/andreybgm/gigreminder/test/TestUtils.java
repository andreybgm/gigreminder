package io.github.andreybgm.gigreminder.test;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestUtils {
    public static <T> boolean assertListContainsAll(List<T> actualList, List<T> expectedList) {
        assertThat(actualList)
                .hasSize(expectedList.size())
                .containsAll(expectedList);

        return true;
    }

    public static <T> boolean assertListContainsAll(List<T> actualList, List<T> expectedList,
                                                    Comparator<T> comparator) {
        assertThat(actualList)
                .hasSize(expectedList.size())
                .usingElementComparator(comparator)
                .containsAll(expectedList);

        return true;
    }

    public static Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }
}
