package org.conetex.contract.runtime.instrument.counter;

public class ArrayStore implements Counter {

    private static boolean isInProgress = false;

    private static ArrayStore head = new ArrayStore();

    private ArrayStore previousCounter = null;

    private long count = Long.MIN_VALUE;

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
            if (head.count == Long.MAX_VALUE) {
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
        head.count = Long.MAX_VALUE;
        head.previousCounter = null;
    }

    public static synchronized ArrayStore getHead() {
        return head;
    }

    public ArrayStore getPrevious() {
        return this.previousCounter;
    }

    public long getCount() {
        return this.count;
    }

}

