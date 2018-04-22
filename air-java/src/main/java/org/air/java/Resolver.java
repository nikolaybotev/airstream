package org.air.java;

import javax.annotation.Nullable;

public interface Resolver<T> {
    void resolve(@Nullable T value);

    void resolve(@Nullable Promise<? extends T> promise);

    void reject(Throwable error);
}
