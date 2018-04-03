package org.air.java.internal;

import org.air.java.Resolver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class ActorReflectiveInvocationMessage implements ActorMessage {
    private final Resolver<Object> resolver;
    private final Supplier<Object> target;
    private final Method method;
    private final Object[] arguments;

    public ActorReflectiveInvocationMessage(Resolver<Object> resolver,
                                            Supplier<Object> target,
                                            Method method,
                                            Object[] arguments) {
        this.resolver = requireNonNull(resolver);
        this.target = requireNonNull(target);
        this.method = requireNonNull(method);
        this.arguments = requireNonNull(arguments);
    }

    @Override
    public void handle() {
        Object result;
        try {
            result = method.invoke(target.get(), arguments);
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
