package com.neil.learning.algorithm;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRU<K, V> extends LinkedHashMap<K, V> {

    private int capacity;

    public LRU(int capacity) {
        this.capacity = capacity;
    }

    protected boolean removeEldestEntry(Map.Entry eldest) {
        return size() > capacity;
    }

    public static void main(String[] args) {
        LRU lru = new LRU(5);
        lru.put("a", "a");
        lru.put("b", "b");
        lru.put("c", "c");
        lru.put("d", "d");
        lru.put("e", "e");
        lru.put("f", "f");

        System.out.println(lru.get("a") == null);
    }
}
