package org.emil.hnrpmc.hnessentials.cosmetics.utils.collections;


import com.mojang.datafixers.util.Either;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Lazy map backed by a hash map.
 */
public class HashMapBackedLazyMap<K, V> implements LazyMap<K, V> {
    public HashMapBackedLazyMap() {
        this.map = new HashMap<>();
    }

    private final Map<K, Either<Supplier<V>, V>> map;
    private int size;

    @Override
    public void put(K key, Supplier<V> value) {
        this.map.put(key, Either.left(value));
    }

    @Override
    public V get(K key) {
        var item = this.map.get(key);

        if (item.left().isPresent()) {
            V value = item.left().get().get();
            this.map.put(key, Either.right(value));
            this.size++;
            return value;
        }
        else {
            return item.right().get();
        }
    }

    @Override
    public boolean containsKey(K key) {
        return this.map.containsKey(key);
    }

    @Override
    public boolean isEvaluated(K key) {
        return this.map.containsKey(key) && this.map.get(key).right().isPresent();
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public int capacity() {
        return this.map.size();
    }
}