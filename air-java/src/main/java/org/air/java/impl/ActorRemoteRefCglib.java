package org.air.java.impl;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.air.java.ActorSystem;
import org.air.java.Future;
import org.air.java.Promise;
import org.air.java.Resolver;
import org.air.java.internal.Actor;
import org.air.java.internal.ActorReflectiveInvocationMessage;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static java.util.Objects.requireNonNull;

public class ActorRemoteRefCglib<T> {
    private static final Resolver<?> NOOP_RESOLVER = new Resolver<>() {
        @Override
        public void resolve(Object value) {
        }

        @Override
        public void resolve(Promise<?> promise) {
        }

        @Override
        public void fail(Throwable error) {
        }
    };

    private final ActorSystem actorSystem;
    private final Actor actor;
    private final T proxy;
    private volatile T target;
    private volatile Throwable error;

    public ActorRemoteRefCglib(ActorSystem actorSystem, Actor actor, Class<T> clazz) {
        this.actorSystem = requireNonNull(actorSystem);
        this.actor = requireNonNull(actor);

        // Create proxy
        Enhancer e = new Enhancer();
        e.setSuperclass(clazz);
        e.setUseFactory(false);
        e.setCallback(new Proxy());
        //noinspection unchecked
        proxy = (T) e.create();
    }

    void setTarget(T target) {
        this.target = target;
    }

    void setError(Throwable error) {
        this.error = error;
    }

    private T getTargetSafely() {
        T target = this.target;
        if (target == null) {
            throw new IllegalStateException("target unresolved");
        }
        Throwable error = this.error;
        if (error != null) {
            throw new IllegalStateException("broken ref", error);
        }
        return target;
    }

    private class Proxy implements MethodInterceptor {
        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            // Validate method
            if (!Modifier.isPublic(method.getModifiers())) {
                throw new IllegalStateException();
            }
            Class<?> returnType = method.getReturnType();
            if (returnType != Void.class && returnType != Promise.class) {
                throw new IllegalStateException();
            }

            Promise<?> promise;
            Resolver<?> resolver;
            if (returnType == Promise.class) {
                Future<?> future = actorSystem.newFuture();
                promise = future.getPromise();
                resolver = future.getResolver();
            } else {
                promise = null;
                resolver = NOOP_RESOLVER;
            }

            actor.postMessage(new ActorReflectiveInvocationMessage<>(actor,
                                                                     resolver,
                                                                     ActorRemoteRefCglib.this::getTargetSafely,
                                                                     method,
                                                                     args));

            return promise;
        }
    }

    public T getRef() {
        return proxy;
    }
}
