package org.conetex.contract.runtime.instrument.counter;

import org.conetex.contract.runtime.instrument.interfaces.Counter;

public class FieldStore extends AbstractCounter {

    private static boolean isInProgress = false;

    private static FieldStore head = new FieldStore();

    static {
        System.out.println("org/conetex/contract/runtime/instrument/counter " + FieldStore.class + " loaded. count: '" + head.count + "'");
    }

    private FieldStore(){}

    public static synchronized void increment() {
        if (isInProgress) {
            // We are already inside increment() → endless recursion detected
            return;
        }
        isInProgress = true;
        try {
            if (head.count == MAX_VALUE) {
                FieldStore newCounter = new FieldStore();
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

    public static synchronized FieldStore getHead() {
        return head;
    }

}

