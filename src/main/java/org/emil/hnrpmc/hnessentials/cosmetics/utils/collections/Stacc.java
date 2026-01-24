package org.emil.hnrpmc.hnessentials.cosmetics.utils.collections;

public interface Stacc<T> {
    T peek();
    void push(T item);
    boolean isEmpty();
    T pop();
    void clear();
}
