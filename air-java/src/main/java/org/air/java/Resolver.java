package org.air.java;

public interface Resolver<T> {
    void resolve(T value);

    void resolve(Promise<? extends T> promise);

    void fail(Throwable error);
}
