package org.air.java.internal;

import static java.util.Objects.requireNonNull;

public abstract class AbstractActorMessage implements ActorMessage {
    private final Actor actor;

    AbstractActorMessage(Actor actor) {
        this.actor = requireNonNull(actor);
    }

    @Override
    public final Actor getActor() {
        return actor;
    }
}
