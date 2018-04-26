package org.air.java;

public interface Future<T> {
    static <T> Future<T> newFuture() {
        return ActorSystem.getCurrent().newFuture();
    }

    Promise<T> getPromise();
    Resolver<T> getResolver();
}
