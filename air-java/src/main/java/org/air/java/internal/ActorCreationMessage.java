package org.air.java.internal;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class ActorCreationMessage<T> extends AbstractActorMessage {
    private final Supplier<T> actorSupplier;
    private final Consumer<T> actorConsumer;
    private final Consumer<Throwable> errorConsumer;

    public ActorCreationMessage(Actor actor,
                                Supplier<T> actorSupplier,
                                Consumer<T> actorConsumer,
                                Consumer<Throwable> errorConsumer) {
        super(actor);
        this.actorSupplier = requireNonNull(actorSupplier);
        this.actorConsumer = requireNonNull(actorConsumer);
        this.errorConsumer = requireNonNull(errorConsumer);
    }

    @Override
    public void handle() {
        T actor;
        try {
            actor = actorSupplier.get();
        } catch (Throwable t) {
            errorConsumer.accept(t);
            return;
        }
        actorConsumer.accept(actor);
    }
}
