package org.air.java.impl;

import com.google.common.collect.ImmutableList;
import org.air.java.Future;
import org.air.java.Promise;
import org.air.java.internal.Actor;
import org.air.java.internal.ActorBootstrapMessage;
import org.air.java.internal.ActorCreationMessage;
import org.air.java.internal.ActorExitMessage;
import org.air.java.internal.FutureImpl;
import org.air.java.internal.InternalActorSystem;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

public class ActorSystemImpl implements InternalActorSystem {
    private final String name;
    private final List<ActorHost> actorHosts;
    private final List<Thread> threads;
    private final CountDownLatch terminationLatch;
    private final AtomicLong actorCounter = new AtomicLong();

    public ActorSystemImpl() {
        this(Runtime.getRuntime(), null, 1d);
    }

    public ActorSystemImpl(Runtime runtime) {
        this(runtime, null, 1d);
    }

    public ActorSystemImpl(Runtime runtime, @Nullable String name) {
        this(runtime, name, 1d);
    }

    public ActorSystemImpl(Runtime runtime, @Nullable String name, double interleaveRatio) {
        requireNonNull(runtime);
        this.name = requireNonNullElse(name, String.format("actor-system-%4x", Objects.hashCode(this)));
        if (interleaveRatio <= 0) {
            throw new IllegalArgumentException("interleaveRatio must be positive");
        }

        int actorHostCount = (int) Math.ceil(runtime.availableProcessors() * interleaveRatio);
        ThreadGroup threadGroup = new ThreadGroup(this.name);
        ImmutableList.Builder<ActorHost> actorHostsBuilder = ImmutableList.builder();
        ImmutableList.Builder<Thread> threadsBuilder = ImmutableList.builder();
        this.terminationLatch = new CountDownLatch(actorHostCount);
        for (int i = 0; i < actorHostCount; i++) {
            ActorHost actorHost = new ActorHost(this, new LinkedBlockingQueue<>(), terminationLatch);
            actorHostsBuilder.add(actorHost);
            String threadName = String.format("%s-%d", this.name, i);
            threadsBuilder.add(new Thread(threadGroup, actorHost, threadName));
        }
        actorHosts = actorHostsBuilder.build();
        threads = threadsBuilder.build();

        for (Thread thread : threads) {
            thread.start();
        }
    }

    @Override
    public void start(Runnable main) {
        ActorHost actorHost = selectActorHost();
        Actor actor = new ActorImpl(actorHost.getMessageConsumer());
        actor.postMessage(new ActorBootstrapMessage(actor, main));
    }

    @Override
    public <T> T newActor(Class<T> clazz, Supplier<T> factory) {
        ActorHost actorHost = selectActorHost();
        Actor actor = new ActorImpl(actorHost.getMessageConsumer());
        ActorRemoteRefCglib<T> ref = new ActorRemoteRefCglib<>(this, actor, clazz);
        actor.postMessage(new ActorCreationMessage<>(actor, factory, ref::setTarget, ref::setError));
        return ref.getRef();
    }

    @Override
    public <T> Future<T> newFuture() {
        return FutureImpl.newFuture(this, getCurrentActor());
    }

    @Override
    public <T> Promise<T> resolved(@Nullable T value) {
        return FutureImpl.newResolvedPromise(this, value);
    }

    @Override
    public <T> Promise<T> rejected(Throwable error) {
        return FutureImpl.newRejectedPromise(this, error);
    }

    @Override
    public void shutdown() {
        for (ActorHost actorHost : actorHosts) {
            Actor stopActor = new ActorImpl(actorHost.getMessageConsumer());
            actorHost.getMessageConsumer().accept(new ActorExitMessage(stopActor));
        }
    }

    @Override
    public void awaitTermination(Duration duration) throws InterruptedException {
        terminationLatch.await(duration.toNanos(), TimeUnit.NANOSECONDS);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Actor getCurrentActor() {
        ActorHost actorHost = ActorHost.getCurrentActorHost();
        if (actorHost == null) {
            throw new IllegalStateException();
        }
        Actor currentActor = actorHost.getCurrentActor();
        if (currentActor == null) {
            throw new IllegalStateException();
        }
        return currentActor;
    }

    private ActorHost selectActorHost() {
        long actorId = actorCounter.getAndIncrement();
        return actorHosts.get((int) (actorId % actorHosts.size()));
    }
}
