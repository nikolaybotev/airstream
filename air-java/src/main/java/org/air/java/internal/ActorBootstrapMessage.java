package org.air.java.internal;

import static java.util.Objects.requireNonNull;

public class ActorBootstrapMessage extends AbstractActorMessage {
    private final Runnable runnable;

    public ActorBootstrapMessage(Actor actor, Runnable runnable) {
        super(actor);
        this.runnable = requireNonNull(runnable);
    }

    @Override
    public void handle() {
        runnable.run();
    }
}
