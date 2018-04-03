package org.air.java;

import java.util.function.Function;

@FunctionalInterface
public interface PromiseFunction<T, V> extends Function<T, Promise<? extends V>> {
    Promise<? extends V> apply(T value);
}
