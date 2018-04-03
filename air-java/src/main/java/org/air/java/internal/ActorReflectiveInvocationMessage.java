package org.air.java.internal;

import org.air.java.Resolver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ActorReflectiveInvocationMessage implements ActorMessage {
    private final Resolver<Object> resolver;
    private final Object target;
    private final Method method;
    private final Object[] arguments;

    public ActorReflectiveInvocationMessage(Resolver<Object> resolver,
                                            Object target,
                                            Method method,
                                            Object[] arguments) {
        this.resolver = resolver;
        this.target = target;
        this.method = method;
        this.arguments = arguments;
    }

    @Override
    public void handle() {
        Object result;
        try {
            result = method.invoke(target, arguments);
        } catch (IllegalAccessException e) {
            resolver.fail(e);
            return;
        } catch (InvocationTargetException e) {
            resolver.fail(e.getTargetException());
            return;
        }
        resolver.resolve(result);
    }
}
