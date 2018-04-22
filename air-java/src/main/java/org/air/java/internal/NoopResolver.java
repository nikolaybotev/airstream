package org.air.java.internal;

import org.air.java.Promise;
import org.air.java.Resolver;

public class NoopResolver<T> implements Resolver<T> {
    public static final Resolver<?> INSTANCE = new NoopResolver<>();

    private NoopResolver() {
        // prevent instantiation
    }

    @Override
    public void resolve(Object value) {
    }

    @Override
    public void resolve(Promise<? extends T> promise) {
    }

    @Override
    public void reject(Throwable error) {
    }
}
