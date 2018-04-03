package org.air.java.internal;

import org.air.java.ActorSystem;

public class InternalActorContext {
    private static final InternalActorContext instance = new InternalActorContext();

    public static InternalActorContext getInstance() {
        return instance;
    }

    private final ThreadLocal<ActorSystem> currentActorSystem = new ThreadLocal<>();
    private final ThreadLocal<Actor> currentActor = new ThreadLocal<>();

    // prevent instantiation
    private InternalActorContext() {}

    public ActorSystem getCurrentActorSystem() {
        return currentActorSystem.get();
    }

    public Actor getCurreActor() {
        return currentActor.get();
    }

    public void setCurrentActorSystem(ActorSystem actorSystem) {
        currentActorSystem.set(actorSystem);
    }

    public void setCurrentActor(Actor actor) {
        currentActor.set(actor);
    }
}
