package org.air.java.internal;

import org.air.java.Resolver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class ActorReflectiveInvocationMessage<T> extends AbstractActorMessage {
    private final Resolver<T> resolver;
    private final Supplier<?> target;
    private final Method method;
    private final Object[] arguments;

    public ActorReflectiveInvocationMessage(Actor actor,
                                            Resolver<T> resolver,
                                            Supplier<?> target,
                                            Method method,
                                            Object[] arguments) {
        super(actor);
        this.resolver = requireNonNull(resolver);
        this.target = requireNonNull(target);
        this.method = requireNonNull(method);
        this.arguments = requireNonNull(arguments);
    }

    @Override
    public void handle() {
        T result;
        try {
            //noinspection unchecked
            result = (T) method.invoke(target.get(), arguments);
        } catch (InvocationTargetException e) {
            resolver.fail(e.getTargetException());
            return;
        } catch (Exception e) {
            resolver.fail(e);
            return;
        }
        resolver.resolve(result);
    }
}
