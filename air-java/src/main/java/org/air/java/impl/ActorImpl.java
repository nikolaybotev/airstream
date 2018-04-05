package org.air.java.impl;

import org.air.java.internal.Actor;
import org.air.java.internal.ActorMessage;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public class ActorImpl implements Actor {
    private final Consumer<ActorMessage> actorMessageConsumer;


    public ActorImpl(Consumer<ActorMessage> actorMessageConsumer) {
        this.actorMessageConsumer = requireNonNull(actorMessageConsumer);
    }

    @Override
    public void postMessage(ActorMessage message) {
        actorMessageConsumer.accept(message);
    }
}
