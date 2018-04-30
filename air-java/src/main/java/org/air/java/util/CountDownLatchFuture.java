package org.air.java.util;

import org.air.java.Future;
import org.air.java.Promise;

public class CountDownLatchFuture {
    private final Future<Void> future;
    private int count;

    public CountDownLatchFuture(int count) {
        if (count < 0) throw new IllegalArgumentException("count < 0");

        future = Future.newFuture();
        setCount(count);
    }

    public Promise<Void> await() {
        return future.getPromise();
    }

    public void countDown() {
        if (count > 0) {
            setCount(count - 1);
        }
    }

    public int getCount() {
        return count;
    }

    private void setCount(int count) {
        this.count = count;
        if (count == 0) {
            future.getResolver().resolve((Void) null);
        }
    }
}
