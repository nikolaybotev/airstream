package org.air.java;

import javax.annotation.Nullable;
import java.util.function.Function;

@FunctionalInterface
public interface PromiseFunction<T, V> extends Function<T, Promise<? extends V>> {
    @Nullable
    Promise<? extends V> apply(@Nullable T value);
}
