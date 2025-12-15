package org.conetex.contract.runtime.instrument.counter;

import org.conetex.contract.runtime.instrument.interfaces.Counter;

public class ArrayNew extends AbstractCounter {

    private static boolean isInProgress = false;

    private static ArrayNew head = new ArrayNew();

    static {
        System.out.println("org/conetex/contract/runtime/instrument/counter " + ArrayNew.class + " loaded. count: '" + head.count + "'");
    }

    private ArrayNew(){}

    public static synchronized void increment() {
        if (isInProgress) {
            // We are already inside increment() → endless recursion detected
            return;
        }
        isInProgress = true;
        try {
            if (head.count == MAX_VALUE) {
                ArrayNew newCounter = new ArrayNew();
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

    public static synchronized ArrayNew getHead() {
        return head;
    }

}

