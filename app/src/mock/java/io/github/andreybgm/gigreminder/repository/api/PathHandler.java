package io.github.andreybgm.gigreminder.repository.api;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.andreybgm.gigreminder.repository.utils.Tree;

public class PathHandler<V> {
    private final Map<Pattern, Values<V>> patterns;

    public PathHandler(Builder<V> builder) {
        this.patterns = builder.patterns;
    }

    public V findValue(String query) {
        for (Pattern pattern: patterns.keySet()) {
            Matcher matcher = pattern.matcher(query);

            if (matcher.matches() && matcher.groupCount() > 0) {
                String key = matcher.group(1);
                String[] keys = new String[matcher.groupCount() - 1];

                for (int i = 2; i <= matcher.groupCount(); i++) {
                    keys[i - 2] = matcher.group(i);
                }

                return patterns.get(pattern).get(key, keys);
            }
        }

        return null;
    }

    public static class Builder<V> {
        private final Map<Pattern, Values<V>> patterns = new HashMap<>();

        public static <V> Builder<V> create() {
            return new Builder<>();
        }

        public Builder<V> patternHandler(Pattern pattern, Values<V> values) {
            patterns.put(pattern, values);
            return this;
        }

        public PathHandler<V> build() {
            return new PathHandler<>(this);
        }
    }

    public static class Values<V> {
        private final Tree<String, V> tree = new Tree<>();

        public static <V> Values<V> create() {
            return new Values<>();
        }

        public Values<V> value(V value, String key, String... keys) {
            tree.put(value, key, keys);
            return this;
        }

        public V get(String key, String... keys) {
            return tree.get(key, keys);
        }
    }
}
