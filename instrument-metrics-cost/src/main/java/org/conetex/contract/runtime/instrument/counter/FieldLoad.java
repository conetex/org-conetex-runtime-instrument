package org.conetex.contract.runtime.instrument.counter;

import org.conetex.contract.runtime.instrument.interfaces.Counter;

public class FieldLoad extends AbstractCounter {

    private static boolean isInProgress = false;

    private static FieldLoad head = new FieldLoad();

    static {
        System.out.println("org/conetex/contract/runtime/instrument/counter " + FieldLoad.class + " loaded. count: '" + head.count + "'");
    }

    private FieldLoad(){}

    public static synchronized void increment() {
        if (isInProgress) {
            // We are already inside increment() → endless recursion detected
            return;
        }
        isInProgress = true;
        try {
            if (head.count == MAX_VALUE) {
                FieldLoad newCounter = new FieldLoad();
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

    public static synchronized FieldLoad getHead() {
        return head;
    }

}

