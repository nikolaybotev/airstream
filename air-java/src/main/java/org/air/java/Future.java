package org.air.java;

public interface Future<T> {
    Promise<T> getPromise();
    Resolver<T> getResolver();
}
