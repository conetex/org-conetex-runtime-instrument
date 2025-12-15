package org.conetex.contract.runtime.instrument.counter;

import org.conetex.contract.runtime.instrument.interfaces.Counter;

public class ExceptionThrow implements Counter {

    private static boolean isInProgress = false;

    private static ExceptionThrow head = new ExceptionThrow();

    private ExceptionThrow previousCounter = null;

    private long count = Long.MIN_VALUE;

    static {
        System.out.println("org/conetex/contract/runtime/instrument/counter " + ExceptionThrow.class + " loaded. count: '" + head.count + "'");
    }

    private ExceptionThrow(){}

    public static synchronized void increment() {
        if (isInProgress) {
            // We are already inside increment() → endless recursion detected
            return;
        }
        isInProgress = true;
        try {
            if (head.count == Long.MAX_VALUE) {
                ExceptionThrow newCounter = new ExceptionThrow();
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

    public static synchronized ExceptionThrow getHead() {
        return head;
    }

    public ExceptionThrow getPrevious() {
        return this.previousCounter;
    }

    public long getCount() {
        return this.count;
    }

}

