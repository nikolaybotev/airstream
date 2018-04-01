package org.air.java;

import java.util.function.Consumer;
import java.util.function.Function;

public interface Promise<T> {
    <V> Promise<V> then(Function<? super T, ? extends V> consumer, Function<Throwable, ? extends V> errorHandler);

    default void then(Consumer<? super T> consumer) {
        then(result -> { consumer.accept(result); return null; }, null);
    }

    default void then(Consumer<? super T> consumer, Consumer<Throwable> errorHandler) {
        then(result -> { consumer.accept(result); return null; },
             error -> { errorHandler.accept(error); return null; });
    }

    default <V> Promise<V> then(Function<? super T, ? extends V> consumer) {
        return then(consumer, null);
    }

    default void trap(Consumer<Throwable> errorHandler) {
        then(null, error -> { errorHandler.accept(error); return null; });
    }

    default <V> Promise<V> trap(Function<Throwable, ? extends V> errorHandler) {
        return then(null, (Function<Throwable, V>) errorHandler::apply);
    }
}
