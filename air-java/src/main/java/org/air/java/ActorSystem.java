package org.air.java;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;

public interface ActorSystem {
    void start(Runnable main);
    <T> T newActor(Class<T> clazz, Supplier<T> factory);
    void shutdown();

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
