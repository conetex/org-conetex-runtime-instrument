package org.conetex.contract.runtime.instrument.counter;

public class ArrayNew implements Counter {

    private static boolean isInProgress = false;

    private static ArrayNew head = new ArrayNew();

    private ArrayNew previousCounter = null;

    private long count = Long.MIN_VALUE;

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
            if (head.count == Long.MAX_VALUE) {
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
        head.count = Long.MAX_VALUE;
        head.previousCounter = null;
    }

    public static synchronized ArrayNew getHead() {
        return head;
    }

    public ArrayNew getPrevious() {
        return this.previousCounter;
    }

    public long getCount() {
        return this.count;
    }

}

