package org.air.java.impl;

import org.air.java.internal.Actor;
import org.air.java.internal.ActorExitException;
import org.air.java.internal.ActorMessage;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public class ActorHost implements Runnable {
    private static final ThreadLocal<ActorHost> currentActorHost = new ThreadLocal<>();

    static ActorHost getCurrentActorHost() {
        return currentActorHost.get();
    }

    private final BlockingQueue<ActorMessage> queue;
    private final Consumer<ActorMessage> messageConsumer;
    private Actor currentActor;

    public ActorHost(BlockingQueue<ActorMessage> queue) {
        this.queue = requireNonNull(queue);
        this.messageConsumer = queue::offer;
    }

    public Consumer<ActorMessage> getMessageConsumer() {
        return messageConsumer;
    }

    public Actor getCurrentActor() {
        return currentActor;
    }

    public void run() {
        currentActorHost.set(this);
        try {
            while (true) {
                ActorMessage message;
                try {
                    message = queue.take();
                } catch (InterruptedException e) {
                    // Ignore interruptions
                    continue;
                }
                currentActor = message.getActor();
                try {
                    message.handle();
                } catch (ActorExitException ex) {
                    break;
                } catch (Throwable t) {
                    // TODO Log error
                } finally {
                    currentActor = null;
                }
            }
        } finally {
            currentActorHost.remove();
        }
    }
}
