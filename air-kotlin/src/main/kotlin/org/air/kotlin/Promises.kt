package org.air.kotlin

import org.air.java.Promise

inline infix fun <T, V> Promise<T>.flatThen(crossinline consumer: (T?) -> Promise<V>): Promise<V> =
        this.flatThen({ consumer(it) }, null)

inline fun <T, V> Promise<T>.flatThen(crossinline consumer: (T?) -> Promise<V>,
                                      crossinline errorHandler: (Throwable?) -> Promise<V>): Promise<V> =
        this.flatThen({ consumer(it) }, { errorHandler(it) })

inline infix fun <T, V> Promise<T>.then(crossinline kotlinFunc: (T?) -> V): Promise<V> =
        this.then({ kotlinFunc(it) }, null)

inline fun <T, V> Promise<T>.then(crossinline kotlinFunc: (T) -> V,
                                  crossinline errorHandler: (Throwable) -> V?): Promise<V> =
        this.then({ kotlinFunc(it) }, { errorHandler(it) })

inline infix fun <T, V> Promise<T>.flatTrap(crossinline kotlinFunc: (Throwable?) -> Promise<V>): Promise<V> =
        this.flatThen(null, { kotlinFunc(it) })

inline infix fun <T, V> Promise<T>.trap(crossinline kotlinFunc: (Throwable) -> V): Promise<V> =
        this.then(null, { kotlinFunc(it) })
