package org.air.java;

import java.util.function.Consumer;
import java.util.function.Function;

public interface Promise<T> {
    <V> Promise<V> then(Function<? super T, ? extends V> consumer, Function<Throwable, ? extends V> errorHandler);

    <V> Promise<V> then(PromiseFunction<? super T, ? extends V> consumer,
                        PromiseFunction<Throwable, ? extends V> errorHandler);

    default void then(Consumer<? super T> consumer) {
        then((Function<T, Void>) result -> { consumer.accept(result); return null; }, null);
    }

    default void then(Consumer<? super T> consumer, Consumer<Throwable> errorHandler) {
        then((Function<T, Void>) result -> { consumer.accept(result); return null; },
             error -> { errorHandler.accept(error); return null; });
    }

    default <V> Promise<V> then(Function<? super T, ? extends V> consumer) {
        return then(consumer, null);
    }

    default <V> Promise<V> then(PromiseFunction<? super T, ? extends V> consumer) {
        return then(consumer, null);
    }

    default void trap(Consumer<Throwable> errorHandler) {
        then(null, (Function<Throwable, Void>) error -> { errorHandler.accept(error); return null; });
    }

    default <V> Promise<V> trap(Function<Throwable, ? extends V> errorHandler) {
        return then(null, errorHandler);
    }

    default <V> Promise<V> trap(PromiseFunction<Throwable, ? extends V> errorHandler) {
        return then(null, errorHandler);
    }
}
