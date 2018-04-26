package org.air.java;

import java.util.function.Supplier;

public class Actors {
    public static <T> T newActor(Class<T> clazz) throws NoSuchMethodException, IllegalAccessException {
        return ActorSystem.getCurrent().newActor(clazz);
    }

    public static <T> T newActor(Class<T> clazz, Supplier<T> factory) {
        return ActorSystem.getCurrent().newActor(clazz, factory);
    }

    private Actors() {
        // prevent instantiation
    }
}
