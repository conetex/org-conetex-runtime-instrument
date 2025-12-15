package org.conetex.contract.runtime.instrument.counter;

import org.conetex.contract.runtime.instrument.interfaces.Counter;

public class ArithmeticAddSubNeg implements Counter {
    public static final float WEIGHT = 0.05f;


    private static boolean isInProgress = false;

    private static ArithmeticAddSubNeg head = new ArithmeticAddSubNeg();

    private ArithmeticAddSubNeg previousCounter = null;

    private long count = Long.MIN_VALUE;

    static {
        System.out.println("org/conetex/contract/runtime/instrument/counter " + ArithmeticAddSubNeg.class + " loaded. count: '" + head.count + "'");
    }

    private ArithmeticAddSubNeg(){}

    /**
     * Increments the counter. This method is designed to track and count costs in the
     * program by increasing the counter value in a thread-safe manner. If the current counter
     * reaches its maximum value, a new counter instance is created and linked to the previous one.
     *
     * Key implementation details:
     * - This method does not use or depend on any external classes or methods outside this class,
     *   except for Java primitives and `synchronized`, which are part of the core language.
     * - All operations are performed only with primitive types (e.g., `long`, `boolean`) or
     *   internal class references (e.g., `head`, `previousCounter`), ensuring independence from
     *   any potentially instrumented classes.
     * - A safeguard (`isInProgress`) is implemented to detect and prevent recursive calls,
     *   which avoids endless recursion and ensures the integrity of the counter state.
     *
     * As a result, this method is safe to use in instrumented environments, as it avoids any
     * circular calls or interference caused by instrumented classes or dependencies.
     */
    public static synchronized void increment() {
        if (isInProgress) {
            // We are already inside increment() → endless recursion detected
            return;
        }
        isInProgress = true;
        try {
            if (head.count == Long.MAX_VALUE) {
                ArithmeticAddSubNeg newCounter = new ArithmeticAddSubNeg();
                newCounter.previousCounter = head;
                head = newCounter;
            } else {
                head.count++;
            }
        } finally {
            isInProgress = false;
        }
    }

    public static synchronized void reset() {
        head.count = Long.MAX_VALUE;
        head.previousCounter = null;
    }

    public static synchronized ArithmeticAddSubNeg getHead() {
        return head;
    }

    public ArithmeticAddSubNeg getPrevious() {
        return this.previousCounter;
    }

    public long getCount() {
        return this.count;
    }

}

