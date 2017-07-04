package io.github.andreybgm.gigreminder.repository.api;

import org.junit.Before;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class PathHandlerTest {
    private PathHandler<String> pathHandler;

    @Before
    public void setUp() throws Exception {
        Pattern countryPattern = Pattern.compile("countries/\\?data=(.+)&code=(.+)");
        Pattern itemPattern = Pattern.compile(".*/items/(\\d+)");

        pathHandler = new PathHandler.Builder<String>()
                .patternHandler(countryPattern, new PathHandler.Values<String>()
                        .value("rub", "currency", "ru")
                        .value("eur", "currency", "fr")
                        .value("russian", "lang", "ru"))
                .patternHandler(itemPattern, new PathHandler.Values<String>()
                        .value("item1", "1111")
                        .value("item2", "2222"))
                .build();
    }

    @Test
    public void findValue() throws Exception {
        assertThat(pathHandler.findValue("countries/?data=currency&code=ru")).isEqualTo("rub");
        assertThat(pathHandler.findValue("countries/?data=currency&code=fr")).isEqualTo("eur");
        assertThat(pathHandler.findValue("countries/?data=lang&code=ru")).isEqualTo("russian");
        assertThat(pathHandler.findValue("countries/?data=lang&code=fr")).isNull();
        assertThat(pathHandler.findValue("/api/1.1/items/1111")).isEqualTo("item1");
        assertThat(pathHandler.findValue("/api/1.1/items/2222")).isEqualTo("item2");
        assertThat(pathHandler.findValue("/api/1.1/items/3333")).isNull();
    }
}