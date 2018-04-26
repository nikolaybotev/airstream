package org.air.java;

import org.air.java.impl.ActorHost;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.function.Supplier;

public interface ActorSystem {
    String getName();

    void start(Runnable main);

    <T> T newActor(Class<T> clazz, Supplier<T> factory);

    <T> Future<T> newFuture();

    <T> Promise<T> resolved(@Nullable T resolution);

    <T> Promise<T> rejected(Throwable reason);

    void shutdown();

    void awaitTermination(Duration duration) throws InterruptedException;

    static ActorSystem getCurrent() {
        return ActorHost.getCurrentActorHost().getActorSystem();
    }

    default <T> T newActor(Class<T> clazz) throws NoSuchMethodException, IllegalAccessException {
        Constructor<T> defaultConstructor = clazz.getDeclaredConstructor();
        if (!Modifier.isPublic(defaultConstructor.getModifiers())) {
            throw new IllegalAccessException(String.format("Default constructor of %s is not public.", clazz));
        }
        return newActor(clazz, () -> {
            try {
                return defaultConstructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Error instantiating actor.", e);
            }
        });
    }
}
