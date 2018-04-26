package org.air.java.impl;

import org.air.java.ActorSystem;
import org.air.java.internal.Actor;
import org.air.java.internal.ActorExitException;
import org.air.java.internal.ActorMessage;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public class ActorHost implements Runnable {
    private static final ThreadLocal<ActorHost> currentActorHost = new ThreadLocal<>();

    public static ActorHost getCurrentActorHost() {
        return currentActorHost.get();
    }

    private final ActorSystem actorSystem;
    private final BlockingQueue<ActorMessage> queue;
    private final CountDownLatch terminationLatch;
    private final Consumer<ActorMessage> messageConsumer;
    private Actor currentActor;

    public ActorHost(ActorSystem actorSystem, BlockingQueue<ActorMessage> queue, CountDownLatch terminationLatch) {
        this.actorSystem = requireNonNull(actorSystem);
        this.queue = requireNonNull(queue);
        this.terminationLatch = requireNonNull(terminationLatch);
        this.messageConsumer = queue::add;
    }

    public Consumer<ActorMessage> getMessageConsumer() {
        return messageConsumer;
    }

    public ActorSystem getActorSystem() {
        return actorSystem;
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
                    System.err.println(t);
                    t.printStackTrace();
                } finally {
                    currentActor = null;
                }
            }
        } finally {
            try {
                currentActorHost.remove();
            } finally {
                terminationLatch.countDown();
            }
        }
    }
}
