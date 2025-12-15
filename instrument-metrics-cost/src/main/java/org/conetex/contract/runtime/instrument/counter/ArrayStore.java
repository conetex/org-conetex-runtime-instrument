package org.conetex.contract.runtime.instrument.counter;

import org.conetex.contract.runtime.instrument.interfaces.Counter;

public class ArrayStore extends AbstractCounter {

    private static boolean isInProgress = false;

    private static ArrayStore head = new ArrayStore();

    static {
        System.out.println("org/conetex/contract/runtime/instrument/counter " + ArrayStore.class + " loaded. count: '" + head.count + "'");
    }

    private ArrayStore(){}

    public static synchronized void increment() {
        if (isInProgress) {
            // We are already inside increment() → endless recursion detected
            return;
        }
        isInProgress = true;
        try {
            if (head.count == MAX_VALUE) {
                ArrayStore newCounter = new ArrayStore();
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

    public static synchronized ArrayStore getHead() {
        return head;
    }

}

