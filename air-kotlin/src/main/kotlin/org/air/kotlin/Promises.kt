package org.air.kotlin

import org.air.java.Promise
import org.air.java.PromiseFunction

inline infix fun <T, V> Promise<T>.flatThen(crossinline consumer: (T?) -> Promise<V>): Promise<V> =
        this.then(PromiseFunction { consumer(it) }, null)

inline fun <T, V> Promise<T>.flatThen(crossinline consumer: (T?) -> Promise<V>,
                                      crossinline errorHandler: (Throwable?) -> Promise<V>): Promise<V> =
        this.then(PromiseFunction { consumer(it) }, PromiseFunction { errorHandler(it) })

inline infix fun <T, V> Promise<T>.then(crossinline kotlinFunc: (T?) -> V): Promise<V> =
        this.then(java.util.function.Function { kotlinFunc(it) }, null)

inline fun <T, V> Promise<T>.then(crossinline kotlinFunc: (T) -> V,
                                  crossinline errorHandler: (Throwable) -> V?): Promise<V> =
        this.then(java.util.function.Function { kotlinFunc(it) }, java.util.function.Function { errorHandler(it) })

inline infix fun <T, V> Promise<T>.flatTrap(crossinline kotlinFunc: (Throwable?) -> Promise<V>): Promise<V> =
        this.then(null, PromiseFunction<Throwable, V> { kotlinFunc(it) })

inline infix fun <T, V> Promise<T>.trap(crossinline kotlinFunc: (Throwable) -> V): Promise<V> =
        this.then(null, java.util.function.Function { kotlinFunc(it) })
