package io.github.andreybgm.gigreminder.repository.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tree<K, V> {
    private Map<K, Node<K, V>> children;

    public Tree() {
        children = new HashMap<>();
    }

    public void put(V value, K key, K... keys) {
        List<K> allKeys = new ArrayList<>();
        allKeys.add(key);
        Collections.addAll(allKeys, keys);

        Map<K, Node<K, V>> currentChildren = children;

        for (int i = 0; i < allKeys.size(); i++) {
            K currentKey = allKeys.get(i);
            Node<K, V> node = currentChildren.get(currentKey);

            if (node == null) {
                node = new Node<>();
                currentChildren.put(currentKey, node);
            }

            if (i == allKeys.size() - 1) {
                node.addValue(value);
            } else {
                node.addLeaf(currentKey);
            }

            currentChildren = node.getChildren();
        }
    }

    public V get(K key, K... keys) {
        List<K> allKeys = new ArrayList<>();
        allKeys.add(key);
        Collections.addAll(allKeys, keys);

        Map<K, Node<K, V>> currentChildren = children;

        for (int i = 0; i < allKeys.size(); i++) {
            K currentKey = allKeys.get(i);
            Node<K, V> node = currentChildren.get(currentKey);

            if (node == null) {
                return null;
            }

            if (i == allKeys.size() - 1) {
                return  node.getValue();
            }

            currentChildren = node.getChildren();
        }

        return null;
    }

    private static class Node<K, V> {
        private V value;
        private Map<K, Node<K, V>> children;

        Node() {
            children = new HashMap<>();
        }

        void addValue(V value) {
            this.value = value;
        }

        void addLeaf(K key) {
            //noinspection Java8CollectionsApi
            if (children.get(key) == null) {
                children.put(key, new Node<>());
            }
        }

        Map<K,Node<K,V>> getChildren() {
            return children;
        }

        V getValue() {
            return value;
        }
    }
}
