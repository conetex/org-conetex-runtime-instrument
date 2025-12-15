package org.conetex.contract.runtime.instrument.counter;

import org.conetex.contract.runtime.instrument.interfaces.Counter;

public class Monitor implements Counter {

    private static boolean isInProgress = false;

    private static Monitor head = new Monitor();

    private Monitor previousCounter = null;

    private long count = Long.MIN_VALUE;

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
            if (head.count == Long.MAX_VALUE) {
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
        head.count = Long.MAX_VALUE;
        head.previousCounter = null;
    }

    public static synchronized Monitor getHead() {
        return head;
    }

    public Monitor getPrevious() {
        return this.previousCounter;
    }

    public long getCount() {
        return this.count;
    }

}

