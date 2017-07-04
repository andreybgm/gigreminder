package io.github.andreybgm.gigreminder.utils;

import org.junit.Test;

import io.github.andreybgm.gigreminder.repository.utils.Tree;

import static org.assertj.core.api.Assertions.assertThat;

public class TreeTest {

    @Test
    public void addValueWithOneKey() throws Exception {
        Tree<String, String> tree = new Tree<>();
        tree.put("value1", "key1");

        assertThat(tree.get("key1")).isEqualTo("value1");
    }

    @Test
    public void addValueWithThreeKeys() throws Exception {
        Tree<String, String> tree = new Tree<>();
        tree.put("value1", "key1", "key2", "key3");

        assertThat(tree.get("key1", "key2", "key3")).isEqualTo("value1");
    }

    @Test
    public void addTwoValues() throws Exception {
        Tree<String, String> tree = new Tree<>();
        tree.put("value1", "key1");
        tree.put("value2", "key2");

        assertThat(tree.get("key1")).isEqualTo("value1");
        assertThat(tree.get("key2")).isEqualTo("value2");
    }

    @Test
    public void addManyValues() throws Exception {
        Tree<String, String> tree = new Tree<>();
        tree.put("value1", "key1", "key11");
        tree.put("value2", "key1", "key12");
        tree.put("value3", "key2");
        tree.put("value4", "key2", "key21");
        tree.put("value5", "key2", "key21", "key211");
        tree.put("value6", "key2", "key21", "key212");

        assertThat(tree.get("key1", "key11")).isEqualTo("value1");
        assertThat(tree.get("key1", "key12")).isEqualTo("value2");
        assertThat(tree.get("key2")).isEqualTo("value3");
        assertThat(tree.get("key2", "key21")).isEqualTo("value4");
        assertThat(tree.get("key2", "key21", "key211")).isEqualTo("value5");
        assertThat(tree.get("key2", "key21", "key212")).isEqualTo("value6");
    }

    @Test
    public void addExistingKey() throws Exception {
        Tree<String, String> tree = new Tree<>();
        tree.put("value1", "key1");
        tree.put("value2", "key1");

        assertThat(tree.get("key1")).isEqualTo("value2");
    }
}
