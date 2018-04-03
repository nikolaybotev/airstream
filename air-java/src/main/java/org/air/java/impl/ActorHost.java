package org.air.java.impl;

import org.air.java.internal.ActorExitException;
import org.air.java.internal.ActorMessage;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public class ActorHost implements Runnable {
    private final BlockingQueue<ActorMessage> queue;
    private final Consumer<ActorMessage> messageConsumer;

    public ActorHost(BlockingQueue<ActorMessage> queue) {
        this.queue = requireNonNull(queue);
        this.messageConsumer = queue::offer;
    }

    public Consumer<ActorMessage> getMessageConsumer() {
        return messageConsumer;
    }

    public void run() {
        while (true) {
            ActorMessage message;
            try {
                message = queue.take();
            } catch (InterruptedException e) {
                // Ignore interruptions
                continue;
            }
            try {
                message.handle();
            } catch (ActorExitException ex) {
                break;
            } catch (Throwable t) {
                // TODO Log error
            }
        }
    }
}
