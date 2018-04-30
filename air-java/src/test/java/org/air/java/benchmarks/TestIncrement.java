package org.air.java.benchmarks;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TestIncrement {
    public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
        // Warm up
        Random rand = new Random();
        long totalElapsed = 0;
        for (int i = 0; i < 5; i++) {
            totalElapsed += warmUp(rand.nextInt(1_000) + 10_000);
        }
        for (int i = 0; i < 10_000; i++) {
            totalElapsed += warmUp(rand.nextInt(4) + 1);
        }
        System.out.printf("Warmed up in %,d ms.%n%n", totalElapsed);

        runTestAndReport(new CounterBare(), "Single thread", 1);
        runTestAndReport(new CounterWithVolatile(), "Single thread with volatile", 1);
        runTestAndReport(new CounterWithCAS(), "Single thread with CAS", 1);
        runTestAndReport(new CounterWithSynchronized(), "Single thread with synchronized", 1);
        runTestAndReport(new CounterWithLock(), "Single thread with lock", 1);
        runTestAndReport(new CounterWithCAS(), "Two threads with CAS", 2);
        runTestAndReport(new CounterWithSynchronized(), "Two threads with synchronized", 2);
        runTestAndReport(new CounterWithLock(), "Two threads with lock", 2);
    }

    private static long warmUp(int iterations) throws InterruptedException, BrokenBarrierException {
        long totalElapsed = 0;
        totalElapsed += runTest(new CounterBare(), 1, iterations);
        totalElapsed += runTest(new CounterWithVolatile(), 1, iterations);
        totalElapsed += runTest(new CounterWithCAS(), 1, iterations);
        totalElapsed += runTest(new CounterWithSynchronized(), 1, iterations);
        totalElapsed += runTest(new CounterWithLock(), 1, iterations);
        totalElapsed += runTest(new CounterWithCAS(), 2, iterations);
        totalElapsed += runTest(new CounterWithSynchronized(), 2, iterations);
        totalElapsed += runTest(new CounterWithLock(), 2, iterations);
        return totalElapsed;
    }

    private static void runTestAndReport(Counter counter, String name, int threadCount)
            throws BrokenBarrierException, InterruptedException {
        long elapsedNanos = runTest(counter, threadCount, 500_000_000);
        System.out.printf("%40s: %,d ms%n", name, TimeUnit.NANOSECONDS.toMillis(elapsedNanos));
    }

    private static long runTest(final Counter counter, final int threadCount, final int iterations)
            throws InterruptedException, BrokenBarrierException {
        final CountDownLatch endLatch = new CountDownLatch(threadCount);
        final int perThreadIterations = iterations / threadCount;

        final CyclicBarrier startBarrier = new CyclicBarrier(threadCount + 1);
        for (int i = 0; i < threadCount; i++) {
            @SuppressWarnings("Convert2Lambda")
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        startBarrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        throw new IllegalStateException(e);
                    }
                    runIterations(counter, perThreadIterations);
                    endLatch.countDown();
                }
            });
            thread.start();
        }

        startBarrier.await();

        long startNanos = System.nanoTime();
        endLatch.await();

        assert counter.getValue() == iterations;

        return System.nanoTime() - startNanos;
    }

    private static void runIterations(Counter c, int n) {
        for (int j = 0; j < n; j++) {
            c.increment();
        }
    }

    private interface Counter {
        void increment();
        long getValue();
    }

    private static class CounterBare implements Counter {
        private long value = 0;

        @Override
        public void increment() {
            value++;
        }

        @Override
        public long getValue() {
            return value;
        }
    }

    private static class CounterWithVolatile implements Counter {
        private volatile long value = 0;

        @SuppressWarnings("NonAtomicOperationOnVolatileField")
        @Override
        public void increment() {
            value++;
        }

        @Override
        public long getValue() {
            return value;
        }
    }

    private static class CounterWithCAS implements Counter {
        private AtomicLong value = new AtomicLong(0);

        @Override
        public void increment() {
            value.incrementAndGet();
        }

        @Override
        public long getValue() {
            return value.get();
        }
    }

    private static class CounterWithSynchronized implements Counter {
        private final Object lock = new Object();
        private long value = 0;

        @Override
        public void increment() {
            synchronized (lock) {
                value++;
            }
        }

        @Override
        public long getValue() {
            return value;
        }
    }

    private static class CounterWithLock implements Counter {
        private final Lock lock = new ReentrantLock();
        private long value = 0;

        @Override
        public void increment() {
            lock.lock();
            try {
                value++;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public long getValue() {
            return value;
        }
    }
}
