package org.air.java;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

public interface Promise<T> {
    static<T> Promise<T> resolved(@Nullable T value) {
        return ActorSystem.getCurrent().resolved(value);
    }

    static<T> Promise<T> rejected(Throwable error) {
        return ActorSystem.getCurrent().rejected(error);
    }

    <V> Promise<V> then(@Nullable Function<? super T, ? extends V> consumer,
                        @Nullable Function<Throwable, ? extends V> errorHandler);

    <V> Promise<V> then(@Nullable PromiseFunction<? super T, ? extends V> consumer,
                        @Nullable PromiseFunction<Throwable, ? extends V> errorHandler);

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
