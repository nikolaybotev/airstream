package org.air.java.internal;

import org.air.java.Resolver;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class ActorPromiseFailureMessage<V> extends AbstractActorMessage {
    private final Resolver<V> resolver;
    private final Function<Throwable, V> handler;
    private final Throwable exception;

    public ActorPromiseFailureMessage(Actor actor,
                                      Resolver<V> resolver,
                                      Function<Throwable, V> handler,
                                      Throwable exception) {
        super(actor);
        this.resolver = requireNonNull(resolver);
        this.handler = requireNonNull(handler);
        this.exception = requireNonNull(exception);
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
