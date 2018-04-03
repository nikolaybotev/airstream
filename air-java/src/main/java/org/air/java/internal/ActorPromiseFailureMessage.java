package org.air.java.internal;

import org.air.java.Resolver;

import java.util.function.Function;

public class ActorPromiseFailureMessage<V> implements ActorMessage {
    private final Resolver<V> resolver;
    private final Function<Throwable, V> handler;
    private final Throwable exception;

    public ActorPromiseFailureMessage(Resolver<V> resolver, Function<Throwable, V> handler, Throwable exception) {
        this.resolver = resolver;
        this.handler = handler;
        this.exception = exception;
    }

    @Override
    public void handle() {
        V result;
        try {
            result = handler.apply(exception);
        } catch (Throwable t) {
            resolver.fail(t);
            return;
        }
        resolver.resolve(result);
    }
}
