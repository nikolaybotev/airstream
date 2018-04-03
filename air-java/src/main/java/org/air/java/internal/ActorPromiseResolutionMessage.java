package org.air.java.internal;

import org.air.java.Resolver;

import java.util.function.Function;

public class ActorPromiseResolutionMessage<T, V> implements ActorMessage {
    private final Resolver<V> resolver;
    private final Function<T, V> handler;
    private final T resolution;

    public ActorPromiseResolutionMessage(Resolver<V> resolver, Function<T, V> handler, T resolution) {
        this.resolver = resolver;
        this.handler = handler;
        this.resolution = resolution;
    }

    @Override
    public void handle() {
        V result;
        try {
            result = handler.apply(resolution);
        } catch (Throwable t) {
            resolver.fail(t);
            return;
        }
        resolver.resolve(result);
    }
}
