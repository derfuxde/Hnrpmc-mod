package org.emil.hnrpmc.hnessentials.cosmetics.utils.collections;


import java.util.function.Supplier;

/**
 * A map that stores key-value pairs as suppliers, constructing the objects only when necessary.
 * @param <K> the key type.
 * @param <V> the value type.
 */
public interface LazyMap<K, V> {
    /**
     * Put a lazy-supplied value in this map.
     * @param key
     * @param value
     */
    void put(K key, Supplier<V> value);

    /**
     * Gets the value for the given key.
     * @param key the key to get a value for.
     * @return the value associated with this key.
     */
    V get(K key);

    /**
     * Returns whether the map currently contains a mapping for the given key. This includes suppliers of the value that haven't been evaluated.
     * @param key the key to test.
     * @return whether there is a mapping for the given key, including unresolved suppliers.
     */
    boolean containsKey(K key);

    /**
     * Returns whether the map currently contains an evaluated mapping for the given key.
     * @param key the key to test.
     * @return whether the map contains a mapping at the given key, and the value for that mapping is evaluated.
     */
    boolean isEvaluated(K key);

    /**
     * Gets the size of this lazy map. This includes only resolved objects.
     * @return the number of objects stored in the map currently. This includes only objects for which the supplier has been evaluated.
     */
    int size();

    /**
     * Gets the current storage capacity of the lazy map.
     * @return the storage capacity of this lazy map. That is: the number of fully evaluated objects it would carry if all entries were fully evaluated.
     */
    int capacity();
}
