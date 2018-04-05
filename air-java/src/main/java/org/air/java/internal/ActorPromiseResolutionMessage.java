package org.air.java.internal;

import org.air.java.Resolver;

import javax.annotation.Nullable;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class ActorPromiseResolutionMessage<T, V> extends AbstractActorMessage {
    private final Resolver<V> resolver;
    private final Function<T, V> handler;
    private final T resolution;

    public ActorPromiseResolutionMessage(Actor actor,
                                         Resolver<V> resolver,
                                         Function<T, V> handler,
                                         @Nullable T resolution) {
        super(actor);
        this.resolver = requireNonNull(resolver);
        this.handler = requireNonNull(handler);
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
