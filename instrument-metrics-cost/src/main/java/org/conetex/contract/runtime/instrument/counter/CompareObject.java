package org.conetex.contract.runtime.instrument.counter;

import org.conetex.contract.runtime.instrument.interfaces.Counter;

public class CompareObject extends AbstractCounter {

    private static boolean isInProgress = false;

    private static CompareObject head = new CompareObject();

    static {
        System.out.println("org/conetex/contract/runtime/instrument/counter " + CompareObject.class + " loaded. count: '" + head.count + "'");
    }

    private CompareObject(){}

    public static synchronized void increment() {
        if (isInProgress) {
            // We are already inside increment() → endless recursion detected
            return;
        }
        isInProgress = true;
        try {
            if (head.count == MAX_VALUE) {
                CompareObject newCounter = new CompareObject();
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

    public static synchronized CompareObject getHead() {
        return head;
    }

}

