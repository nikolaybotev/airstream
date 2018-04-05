package org.air.java.internal;

public interface ActorMessage {
    Actor getActor();
    void handle();
}
