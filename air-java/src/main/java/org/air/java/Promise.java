package org.air.java;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Promise<T> {
    static<T> Promise<T> resolved(@Nullable T value) {
        return ActorSystem.getCurrent().resolved(value);
    }

    static<T> Promise<T> rejected(Throwable error) {
        return ActorSystem.getCurrent().rejected(error);
    }

    <V> Promise<V> then(@Nullable Function<? super T, ? extends V> consumer,
                        @Nullable Function<Throwable, ? extends V> errorHandler);

    <V> Promise<V> flatThen(@Nullable Function<? super T, Promise<? extends V>> consumer,
                            @Nullable Function<Throwable, Promise<? extends V>> errorHandler);

    default <V> Promise<V> then(Function<? super T, ? extends V> consumer) {
        return then(consumer, null);
    }

    default <V> Promise<V> flatThen(Function<? super T, Promise<? extends V>> consumer) {
        return flatThen(consumer, null);
    }

    default <V> Promise<V> flatThen(Supplier<Promise<? extends V>> consumer) {
        return flatThen(result -> consumer.get(), null);
    }

    default Promise<Void> then(Runnable action) {
        return then(x -> { action.run(); return null; }, null);
    }

    default <V> Promise<V> then(Supplier<V> action) {
        return then(x -> action.get(), null);
    }

    default void trap(Consumer<Throwable> errorHandler) {
        then(null, (Function<Throwable, Void>) error -> { errorHandler.accept(error); return null; });
    }

    default <V> Promise<V> trap(Function<Throwable, ? extends V> errorHandler) {
        return then(null, errorHandler);
    }

    default <V> Promise<V> flatTrap(Function<Throwable, Promise<? extends V>> errorHandler) {
        return flatThen(null, errorHandler);
    }
}
