package org.conetex.contract.runtime.instrument.counter;

import org.conetex.contract.runtime.instrument.interfaces.Counter;

public class CompareLong implements Counter {

    private static boolean isInProgress = false;

    private static CompareLong head = new CompareLong();

    private CompareLong previousCounter = null;

    private long count = Long.MIN_VALUE;

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
            if (head.count == Long.MAX_VALUE) {
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
        head.count = Long.MAX_VALUE;
        head.previousCounter = null;
    }

    public static synchronized CompareLong getHead() {
        return head;
    }

    public CompareLong getPrevious() {
        return this.previousCounter;
    }

    public long getCount() {
        return this.count;
    }

}

