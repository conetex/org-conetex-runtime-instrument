package org.conetex.contract.runtime.instrument.counter;

import org.conetex.contract.runtime.instrument.interfaces.Counter;

public class MethodCall extends AbstractCounter {

    private static boolean isInProgress = false;

    private static MethodCall head = new MethodCall();

    static {
        System.out.println("org/conetex/contract/runtime/instrument/counter " + MethodCall.class + " loaded. count: '" + head.count + "'");
    }

    private MethodCall(){}

    public static synchronized void increment() {
        if (isInProgress) {
            // We are already inside increment() → endless recursion detected
            return;
        }
        isInProgress = true;
        try {
            if (head.count == MAX_VALUE) {
                MethodCall newCounter = new MethodCall();
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
        head.count = MIN_VALUE;
        head.previousCounter = null;
    }

    public static synchronized MethodCall getHead() {
        return head;
    }

}

