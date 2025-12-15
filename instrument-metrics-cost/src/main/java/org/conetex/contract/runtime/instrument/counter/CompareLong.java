package org.conetex.contract.runtime.instrument.counter;

import org.conetex.contract.runtime.instrument.interfaces.Counter;

public class CompareLong extends AbstractCounter {

    private static boolean isInProgress = false;

    private static CompareLong head = new CompareLong();

    static {
        System.out.println("org/conetex/contract/runtime/instrument/counter " + CompareLong.class + " loaded. count: '" + head.count + "'");
    }

    private CompareLong(){}

    public static synchronized void increment() {
        if (isInProgress) {
            // We are already inside increment() → endless recursion detected
            return;
        }
        isInProgress = true;
        try {
            if (head.count == MAX_VALUE) {
                CompareLong newCounter = new CompareLong();
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

    public static synchronized CompareLong getHead() {
        return head;
    }

}

