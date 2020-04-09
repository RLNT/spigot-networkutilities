package rlnt.networkutilities.proxy.utils;

/**
 * Interface for a TriConsumer.
 * Represents an operation that accepts three input arguments and returns no result.
 *
 * @param <T>
 * @param <U>
 * @param <V>
 */
@FunctionalInterface
public interface TriConsumer<T, U, V> {
    void accept(T t, U u, V v);
}
