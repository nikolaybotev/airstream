package org.air.java.benchmarks;

import org.air.java.ActorSystem;
import org.air.java.Actors;
import org.air.java.Promise;
import org.air.java.impl.ActorSystemImpl;
import org.air.java.util.CountDownLatchFuture;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class TestIncrementActors {
    private static final ActorSystem actorSystem = new ActorSystemImpl();

    public static void main(String[] args) {
        actorSystem.start(() -> {
            Counter c = Actors.newActor(Counter.class, Counter::new);

            runTest(c, "Single actor", 1)
                    //.then(r -> runTest(c, "Two actors", 2))
                    .then(actorSystem::shutdown);
        });
    }

    private static Promise<Void> runTest(Counter c, String name, int actorCount) {
        CountDownLatchFuture endLatch = new CountDownLatchFuture(actorCount);
        final int warmUpIterations = 100_000;
        final int iterations = 500_000_000;
        final int perActorIterations = iterations / actorCount;

        CountDownLatchFuture startLatch = new CountDownLatchFuture(actorCount);
        for (int i = 0; i < actorCount; i++) {
            CounterCaller actor = actorSystem.newActor(CounterCaller.class, CounterCaller::new);
            actor.runIterations(c, warmUpIterations).then(() -> {
                startLatch.countDown();
                startLatch.await()
                        .flatThen(x -> actor.runIterations(c, perActorIterations))
                        .then(endLatch::countDown);
                return null;
            });
        }

        return startLatch.await().flatThen(result -> {
            long startNanos = System.nanoTime();
            return endLatch.await().then(() -> {
                long elapsedNanos = System.nanoTime() - startNanos;
                System.out.printf("%20s: %,d ms%n", name, TimeUnit.NANOSECONDS.toMillis(elapsedNanos));
            });
        });
    }

    public static class CounterCaller {
        public Promise<Long> runIterations(Counter c, int n) {
            for (int i = 0; i < n; i++) {
                c.increment();
            }
            return c.getValue();
        }
    }

    public static class Counter {
        private long value;

        public void increment() {
            value++;
        }

        public Promise<Long> getValue() {
            return Promise.resolved(value);
        }
    }
}
