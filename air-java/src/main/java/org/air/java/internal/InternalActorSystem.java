package org.air.java.internal;

import org.air.java.ActorSystem;

public interface InternalActorSystem extends ActorSystem {
    Actor getCurrentActor();
}
