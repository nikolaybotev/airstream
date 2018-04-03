package org.air.java.internal;

public class ActorExitMessage implements ActorMessage {
    @Override
    public void handle() {
        throw new ActorExitException();
    }
}
