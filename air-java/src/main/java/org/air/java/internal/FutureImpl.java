package org.air.java.internal;

import org.air.java.Future;
import org.air.java.Promise;
import org.air.java.Resolver;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class FutureImpl<T> implements Future<T> {
    private final Promise<T> promise;
    private final Resolver<T> resolver;

    public static <T> Future<T> newFuture(InternalActorSystem actorSystem, Actor actor) {
        return new FutureImpl<>(actorSystem, actor);
    }

    public static <T> Promise<T> newResolvedPromise(InternalActorSystem actorSystem, @Nullable T value) {
        return new PromiseImpl<>(actorSystem, value);
    }

    public static <T> Promise<T> newRejectedPromise(InternalActorSystem actorSystem, Throwable error) {
        return new PromiseImpl<>(actorSystem, error);
    }

    private FutureImpl(InternalActorSystem actorSystem, Actor actor) {
        PromiseImpl<T> promise = new PromiseImpl<>(actorSystem);
        this.promise = promise;
        this.resolver = new ResolverImpl<>(actor, promise);
    }

    @Override
    public Promise<T> getPromise() {
        return promise;
    }

    @Override
    public Resolver<T> getResolver() {
        return resolver;
    }

    private static class PromiseImpl<T> implements Promise<T> {
        private final InternalActorSystem actorSystem;
        private List<ActorMessage> resolutionListeners;
        private List<ActorMessage> rejectionListeners;
        private boolean resolved;
        private boolean rejected;
        private T value;
        private Throwable error;

        PromiseImpl(InternalActorSystem actorSystem) {
            this.actorSystem = actorSystem;
            this.resolutionListeners = new ArrayList<>();
            this.rejectionListeners = new ArrayList<>();
        }

        PromiseImpl(InternalActorSystem actorSystem, @Nullable T value) {
            this.actorSystem = actorSystem;
            this.value = value;
            this.resolved = true;
        }

        PromiseImpl(InternalActorSystem actorSystem, Throwable error) {
            this.actorSystem = actorSystem;
            this.error = error;
            this.rejected = true;
        }

        @Override
        public <V> Promise<V> then(@Nullable Function<? super T, ? extends V> consumer,
                                   @Nullable Function<Throwable, ? extends V> errorHandler) {
            if (resolved) {
                // Resolved
                if (consumer == null) {
                    // Error handler will not be invoked and a default consumer will return null...
                    return actorSystem.resolved(null);
                }

                Actor currentActor = actorSystem.getCurrentActor();
                Future<V> future = new FutureImpl<>(actorSystem, currentActor);
                currentActor.postMessage(new ResolutionListenerMessage<>(currentActor, future.getResolver(), consumer));
                return future.getPromise();
            } else if (rejected) {
                // Rejected
                if (errorHandler == null) {
                    // Consumer will not be invoked and a default errorHandler will propagate the exception
                    return actorSystem.rejected(error);
                }

                Actor currentActor = actorSystem.getCurrentActor();
                Future<V> future = new FutureImpl<>(actorSystem, currentActor);
                currentActor.postMessage(
                        new RejectionListenerMessage<>(currentActor, future.getResolver(), errorHandler));
                return future.getPromise();
            } else {
                // Unresolved
                Actor currentActor = actorSystem.getCurrentActor();
                Future<V> future = new FutureImpl<>(actorSystem, currentActor);
                if (consumer != null) {
                    resolutionListeners.add(
                            new ResolutionListenerMessage<>(currentActor, future.getResolver(), consumer));
                } else {
                    resolutionListeners.add(
                            new ResolutionListenerMessage<>(currentActor, future.getResolver(), v -> null));
                }
                if (errorHandler != null) {
                    rejectionListeners.add(
                            new RejectionListenerMessage<>(currentActor, future.getResolver(), errorHandler));
                } else {
                    rejectionListeners.add(
                            new RejectionPropagationMessage<>(currentActor, future.getResolver()));
                }
                return future.getPromise();
            }
        }

        @Override
        public <V> Promise<V> flatThen(@Nullable Function<? super T, Promise<? extends V>> consumer,
                                       @Nullable Function<Throwable, Promise<? extends V>> errorHandler) {
            if (resolved) {
                // Resolved
                if (consumer == null) {
                    // Error handler will not be invoked and a default consumer will return null...
                    return actorSystem.resolved(null);
                }

                Actor currentActor = actorSystem.getCurrentActor();
                Future<V> future = new FutureImpl<>(actorSystem, currentActor);
                currentActor.postMessage(
                        new ResolutionPromiseListenerMessage<>(currentActor, future.getResolver(), consumer));
                return future.getPromise();
            } else if (rejected) {
                // Rejected
                if (errorHandler == null) {
                    // Consumer will not be invoked and a default errorHandler will propagate the exception
                    return actorSystem.rejected(error);
                }

                Actor currentActor = actorSystem.getCurrentActor();
                Future<V> future = new FutureImpl<>(actorSystem, currentActor);
                currentActor.postMessage(
                        new RejectionPromiseListenerMessage<>(currentActor, future.getResolver(), errorHandler));
                return future.getPromise();
            } else {
                // Unresolved
                Actor currentActor = actorSystem.getCurrentActor();
                Future<V> future = new FutureImpl<>(actorSystem, currentActor);
                if (consumer != null) {
                    resolutionListeners.add(
                            new ResolutionPromiseListenerMessage<>(currentActor, future.getResolver(), consumer));
                } else {
                    resolutionListeners.add(
                            new ResolutionPromiseListenerMessage<>(currentActor, future.getResolver(), v -> null));
                }
                if (errorHandler != null) {
                    rejectionListeners.add(
                            new RejectionPromiseListenerMessage<>(currentActor, future.getResolver(), errorHandler));
                } else {
                    rejectionListeners.add(
                            new RejectionPropagationMessage<>(currentActor, future.getResolver()));
                }
                return future.getPromise();
            }
        }

        void resolve(@Nullable T value) {
            if (resolved || rejected) {
                return; // no-op
            }

            this.value = value;
            Actor currentActor = actorSystem.getCurrentActor();
            for (ActorMessage message : resolutionListeners) {
                currentActor.postMessage(message);
            }

            this.resolved = true;
            this.resolutionListeners = null;
            this.rejectionListeners = null;
        }

        void reject(Throwable error) {
            if (resolved || rejected) {
                return; // no-op
            }

            this.error = error;
            Actor currentActor = actorSystem.getCurrentActor();
            for (ActorMessage message : rejectionListeners) {
                currentActor.postMessage(message);
            }

            this.rejected = true;
            this.resolutionListeners = null;
            this.rejectionListeners = null;
        }

        Promise<T> getRefFor(Actor actor) {
            if (resolved || rejected) {
                return this;
            }

            Future<T> future = new FutureImpl<>(actorSystem, actor);
            Resolver<T> resolver = future.getResolver();
            chainTo(actor, resolver);
            return future.getPromise();
        }

        void chainTo(Actor actor, Resolver<? super T> resolver) {
            if (resolved) {
                resolver.resolve(value);
            } else if (rejected) {
                actor.postMessage(new RejectionPropagationMessage<>(actor, resolver));
            } else {
                // Unresolved
                resolutionListeners.add(new ResolutionPropagationMessage(actor, resolver));
                rejectionListeners.add(new RejectionPropagationMessage<>(actor, resolver));
            }
        }

        private class ResolutionListenerMessage<V> extends AbstractActorMessage {
            private final Resolver<V> resolver;
            private final Function<? super T, ? extends V> handler;

            ResolutionListenerMessage(Actor actor,
                                      Resolver<V> resolver,
                                      Function<? super T, ? extends V> handler) {
                super(actor);
                this.resolver = requireNonNull(resolver);
                this.handler = requireNonNull(handler);
            }

            @Override
            public void handle() {
                V result;
                try {
                    result = handler.apply(value);
                } catch (Throwable t) {
                    resolver.reject(t);
                    return;
                }
                resolver.resolve(result);
            }
        }

        private class ResolutionPropagationMessage extends AbstractActorMessage {
            private final Resolver<? super T> resolver;

            ResolutionPropagationMessage(Actor actor, Resolver<? super T> resolver) {
                super(actor);
                this.resolver = requireNonNull(resolver);
            }

            @Override
            public void handle() {
                resolver.resolve(value);
            }
        }

        private class ResolutionPromiseListenerMessage<V> extends AbstractActorMessage {
            private final Resolver<V> resolver;
            private final Function<? super T, Promise<? extends V>> handler;

            ResolutionPromiseListenerMessage(Actor actor,
                                             Resolver<V> resolver,
                                             Function<? super T, Promise<? extends V>> handler) {
                super(actor);
                this.resolver = requireNonNull(resolver);
                this.handler = requireNonNull(handler);
            }

            @Override
            public void handle() {
                resolver.resolve(handler.apply(value));
            }
        }

        private class RejectionListenerMessage<V> extends AbstractActorMessage {
            private final Resolver<V> resolver;
            private final Function<Throwable, ? extends V> handler;

            RejectionListenerMessage(Actor actor,
                                     Resolver<V> resolver,
                                     Function<Throwable, ? extends V> handler) {
                super(actor);
                this.resolver = requireNonNull(resolver);
                this.handler = requireNonNull(handler);
            }

            @Override
            public void handle() {
                V result;
                try {
                    result = handler.apply(error);
                } catch (Throwable t) {
                    resolver.reject(t);
                    return;
                }
                resolver.resolve(result);
            }
        }

        private class RejectionPropagationMessage<V> extends AbstractActorMessage {
            private final Resolver<V> resolver;

            RejectionPropagationMessage(Actor actor, Resolver<V> resolver) {
                super(actor);
                this.resolver = requireNonNull(resolver);
            }

            @Override
            public void handle() {
                resolver.reject(error);
            }
        }

        private class RejectionPromiseListenerMessage<V> extends AbstractActorMessage {
            private final Resolver<V> resolver;
            private final Function<Throwable, Promise<? extends V>> handler;

            RejectionPromiseListenerMessage(Actor actor,
                                            Resolver<V> resolver,
                                            Function<Throwable, Promise<? extends V>> handler) {
                super(actor);
                this.resolver = requireNonNull(resolver);
                this.handler = requireNonNull(handler);
            }

            @Override
            public void handle() {
                resolver.resolve(handler.apply(error));
            }
        }
    }

    private static class PromiseResolutionMessage<T> extends AbstractActorMessage {
        private final PromiseImpl<T> promise;
        private final T value;

        PromiseResolutionMessage(Actor actor, PromiseImpl<T> promise, @Nullable T value) {
            super(actor);
            this.promise = promise;
            this.value = value;
        }

        @Override
        public void handle() {
            promise.resolve(value);
        }
    }

    private static class PromiseRejectionMessage<T> extends AbstractActorMessage {
        private final PromiseImpl<T> promise;
        private final Throwable error;

        PromiseRejectionMessage(Actor actor, PromiseImpl<T> promise, Throwable error) {
            super(actor);
            this.promise = promise;
            this.error = error;
        }

        @Override
        public void handle() {
            promise.reject(error);
        }
    }

    private static class ResolverImpl<T> implements Resolver<T> {
        private final Actor actor;
        private final WeakReference<PromiseImpl<T>> promiseWeakRef;

        ResolverImpl(Actor actor, PromiseImpl<T> promiseWeakRef) {
            this.actor = actor;
            this.promiseWeakRef = new WeakReference<>(promiseWeakRef);
        }

        @Override
        public void resolve(@Nullable T value) {
            PromiseImpl<T> promise = promiseWeakRef.get();
            if (promise == null) {
                return;
            }

            actor.postMessage(new PromiseResolutionMessage<>(actor, promise, value));
            promiseWeakRef.clear();
        }

        @Override
        public void resolve(@Nullable Promise<? extends T> promise) {
            if (promise == null) {
                resolve((T) null);
                return;
            }

            if (promise == promiseWeakRef.get()) {
                throw new IllegalStateException("Promise cannot be resolved to itself");
            }

            //noinspection unchecked
            ((PromiseImpl<? extends T>) promise).chainTo(actor, this);
        }

        @Override
        public void reject(Throwable error) {
            PromiseImpl<T> promise = promiseWeakRef.get();
            if (promise == null) {
                return;
            }

            actor.postMessage(new PromiseRejectionMessage<>(actor, promise, error));
            promiseWeakRef.clear();
        }
    }
}
