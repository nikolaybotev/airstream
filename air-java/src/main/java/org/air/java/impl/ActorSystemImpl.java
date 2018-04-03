package org.air.java.impl;

import org.air.java.ActorSystem;

import java.util.function.Supplier;

public class ActorSystemImpl implements ActorSystem {
    @Override
    public void start(Runnable main) {

    }

    @Override
    public <T> T newActor(Class<T> clazz, Supplier<T> factory) {
        return null;
    }

    @Override
    public void shutdown() {

    }
}
