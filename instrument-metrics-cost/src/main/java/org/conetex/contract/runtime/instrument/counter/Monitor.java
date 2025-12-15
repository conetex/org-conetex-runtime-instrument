package org.conetex.contract.runtime.instrument.counter;

import org.conetex.contract.runtime.instrument.interfaces.Counter;

public class Monitor extends AbstractCounter {

    private static boolean isInProgress = false;

    private static Monitor head = new Monitor();

    static {
        System.out.println("org/conetex/contract/runtime/instrument/counter " + Monitor.class + " loaded. count: '" + head.count + "'");
    }

    private Monitor(){}

    public static synchronized void increment() {
        if (isInProgress) {
            // We are already inside increment() → endless recursion detected
            return;
        }
        isInProgress = true;
        try {
            if (head.count == MAX_VALUE) {
                Monitor newCounter = new Monitor();
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

    public static synchronized Monitor getHead() {
        return head;
    }

}

