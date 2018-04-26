package org.air.java.examples;

import org.air.java.ActorSystem;
import org.air.java.Actors;
import org.air.java.Promise;
import org.air.java.impl.ActorSystemImpl;

import java.time.Duration;

public class HelloWorld {
    public static void main(String[] args) {
        ActorSystem actorSystem = new ActorSystemImpl();

        actorSystem.start(() -> {
            Counter c = Actors.newActor(Counter.class, Counter::new);
            runTest(c, 50_000).then(r -> {
                System.out.println("Finished warmup");
                long startTime = System.currentTimeMillis();
                runTest(c, 500_000_000).then(x -> {
                    long endTime = System.currentTimeMillis();
                    System.out.printf("Took %s %n", Duration.ofMillis(endTime - startTime));
                    actorSystem.shutdown();
                    return null;
                });
                return null;
            });
        });
    }

    private static Promise<?> runTest(Counter c, int n) {
        for (int i = 0; i < n; i++) {
            c.increment();
        }
        return c.getValue().then(x -> {
            System.out.println(x);
            return null;
        });
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
