package org.conetex.runtime.instrument.counter;

import org.conetex.runtime.instrument.interfaces.counter.Incrementable;
import org.conetex.runtime.instrument.interfaces.counter.LinkedLong;
import org.conetex.runtime.instrument.interfaces.counter.LongLimitsConfiguration;

public class Counter implements Incrementable {

    private Node top;

    private boolean incrementationBlocked = false;

    private final LongLimitsConfiguration minMax;

    public Counter(LongLimitsConfiguration config, boolean blockIncrement){
        this.minMax = config;
        this.top = new Node(new Tail(this.minMax), this.minMax);
        this.incrementationBlocked = blockIncrement;
    }

    @Override
    public final synchronized void blockIncrement(boolean incrementationBlocked) {
        this.incrementationBlocked = incrementationBlocked;
    }

    @Override
    public final synchronized LinkedLong peek() {
        return new Node(this.top);
    }

    @Override
    public final synchronized void reset() {
        this.top = new Node(new Tail(this.minMax), this.minMax);
    }

    /**
     * Increments the counter. This method is designed to track and count costs in the
     * program by increasing the counter value in a thread-safe manner. If the current counter
     * reaches its maximum value, a new counter instance is created and linked to the previous one.
     * Key implementation details:
     * - This method does not use or depend on any external classes or methods outside this class,
     *   except for Java primitives and `synchronized`, which are part of the core language.
     * - All operations are performed only with primitive types (e.g., `long`, `boolean`) or
     *   internal class references (e.g., `head`, `previousCounter`), ensuring independence from
     *   any potentially instrumented classes.
     * - A safeguard (`isInProgress`) is implemented to detect and prevent recursive calls,
     *   which avoids endless recursion and ensures the integrity of the counter state.
     * As a result, this method is safe to use in instrumented environments, as it avoids any
     * circular calls or interference caused by instrumented classes or dependencies.
     */
    @Override
    public final synchronized void increment() {
        if (this.incrementationBlocked) {
            // We are already inside increment() → endless recursion detected
            return;
        }
        this.incrementationBlocked = true;
        try {
            if (this.top.value == this.minMax.max()) {
                this.top = new Node(this.top, this.minMax);
            }
            this.top.value++;
        } finally {
            this.incrementationBlocked = false;
        }
    }

}