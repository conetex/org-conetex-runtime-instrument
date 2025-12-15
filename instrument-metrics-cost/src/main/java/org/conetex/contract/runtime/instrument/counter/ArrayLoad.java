package org.conetex.contract.runtime.instrument.counter;

import org.conetex.contract.runtime.instrument.interfaces.Counter;

public class ArrayLoad extends AbstractCounter {

    private static boolean isInProgress = false;

    private static ArrayLoad head = new ArrayLoad();

    static {
        System.out.println("org/conetex/contract/runtime/instrument/counter " + ArrayLoad.class + " loaded. count: '" + head.count + "'");
    }

    private ArrayLoad(){}

    public static synchronized void increment() {
        if (isInProgress) {
            // We are already inside increment() → endless recursion detected
            return;
        }
        isInProgress = true;
        try {
            if (head.count == MAX_VALUE) {
                ArrayLoad newCounter = new ArrayLoad();
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

    public static synchronized ArrayLoad getHead() {
        return head;
    }

}

