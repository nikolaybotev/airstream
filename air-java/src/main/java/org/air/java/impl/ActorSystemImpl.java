package org.air.java.impl;

import org.air.java.ActorSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class ActorSystemImpl implements ActorSystem {
    private final AtomicLong actorCounter = new AtomicLong();
    private final List<ActorHost> actorHosts;

    public ActorSystemImpl(Runtime runtime) {
        int actorHostCount = runtime.availableProcessors();
        actorHosts = new ArrayList<>();
        for (int i = 0; i < actorHostCount; i++) {
            actorHosts.add(new ActorHost(new LinkedBlockingQueue<>()));
        }
    }

    @Override
    public void start(Runnable main) {
        long actorId = actorCounter.getAndIncrement();
        ActorHost actorHost = actorHosts.get((int) (actorId % actorHosts.size()));

    }

    @Override
    public <T> T newActor(Class<T> clazz, Supplier<T> factory) {
        return null;
    }

    @Override
    public void shutdown() {
    }
}
