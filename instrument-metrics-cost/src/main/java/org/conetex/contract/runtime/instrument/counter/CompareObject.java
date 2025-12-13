package org.conetex.contract.runtime.instrument.counter;

public class CompareObject implements Counter {

    private static boolean isInProgress = false;

    private static CompareObject head = new CompareObject();

    private CompareObject previousCounter = null;

    private long count = Long.MIN_VALUE;

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
            if (head.count == Long.MAX_VALUE) {
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
        head.count = Long.MAX_VALUE;
        head.previousCounter = null;
    }

    public static synchronized CompareObject getHead() {
        return head;
    }

    public CompareObject getPrevious() {
        return this.previousCounter;
    }

    public long getCount() {
        return this.count;
    }

}

