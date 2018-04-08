package org.air.java.internal;

public class ActorExitMessage extends AbstractActorMessage {
    ActorExitMessage(Actor actor) {
        super(actor);
    }

    @Override
    public void handle() {
        throw new ActorExitException();
    }
}
