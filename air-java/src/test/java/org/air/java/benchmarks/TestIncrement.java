package org.air.java.benchmarks;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class TestIncrement {
    public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
        runTest(new CounterBare(), "Single thread", 1);
        runTest(new CounterWithVolatile(), "Single thread with volatile", 1);
        runTest(new CounterWithCAS(), "Single thread with CAS", 1);
        runTest(new CounterWithSynchronized(), "Single thread with synchronized", 1);
        runTest(new CounterWithLock(), "Single thread with lock", 1);
        runTest(new CounterWithCAS(), "Two threads with CAS", 2);
        runTest(new CounterWithSynchronized(), "Two threads with synchronized", 2);
        runTest(new CounterWithLock(), "Two threads with lock", 2);
    }

    private static void runTest(Counter counter, String name, int threadCount)
            throws InterruptedException, BrokenBarrierException {
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        int iterations = 500_000_000;
        int perThreadIterations = iterations / threadCount;

        CyclicBarrier startBarrier = new CyclicBarrier(threadCount + 1);
        for (int i = 0; i < threadCount; i++) {
            Thread thread = new Thread(() -> {
                for (int a = 0; a < 100_000; a++) {
                    runIterations(counter, 5);
                }
                for (int a = 0; a < 5; a++) {
                    runIterations(counter, 100_000);
                }
                try {
                    startBarrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    throw new IllegalStateException(e);
                }
                runIterations(counter, perThreadIterations);
                endLatch.countDown();
            });
            thread.start();
        }

        startBarrier.await();

        long startNanos = System.nanoTime();
        endLatch.await();

        long elapsedNanos = System.nanoTime() - startNanos;
        System.out.printf("%40s [%d]: %,d ms%n", name, counter.getValue(), TimeUnit.NANOSECONDS.toMillis(elapsedNanos));
    }

    private static void runIterations(Supplier<Runnable> incBare, int iterations) {
        for (int j = 0; j < iterations; j++) {
            incBare.get().run();
        }
    }

    private interface Counter extends Supplier<Runnable> {
        long getValue();
    }

    private static class CounterBare implements Counter {
        private long value = 0;
        private final Runnable task = () -> value++;

        @Override
        public Runnable get() {
            return task;
        }

        @Override
        public long getValue() {
            return value;
        }
    }

    private static class CounterWithVolatile implements Counter {
        private volatile long value = 0;
        @SuppressWarnings("NonAtomicOperationOnVolatileField")
        private final Runnable task = () -> value++;

        @Override
        public Runnable get() {
            return task;
        }

        @Override
        public long getValue() {
            return value;
        }
    }

    private static class CounterWithCAS implements Counter {
        private AtomicLong value = new AtomicLong(0);
        private final Runnable task = () -> value.incrementAndGet();

        @Override
        public Runnable get() {
            return task;
        }

        @Override
        public long getValue() {
            return value.get();
        }
    }

    private static class CounterWithSynchronized implements Counter {
        private final Object lock = new Object();
        private long value = 0;
        private final Runnable task = () -> { synchronized (lock) { value++; } };

        @Override
        public Runnable get() {
            return task;
        }

        @Override
        public long getValue() {
            return value;
        }
    }

    private static class CounterWithLock implements Counter {
        private final Lock lock = new ReentrantLock();
        private long value = 0;
        private final Runnable task = () -> { lock.lock(); try { value++; } finally { lock.unlock(); } };

        @Override
        public Runnable get() {
            return task;
        }

        @Override
        public long getValue() {
            return value;
        }
    }
}
