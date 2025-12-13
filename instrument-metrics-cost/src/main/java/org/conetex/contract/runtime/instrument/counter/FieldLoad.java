package org.conetex.contract.runtime.instrument.counter;

public class FieldLoad implements Counter {

    private static boolean isInProgress = false;

    private static FieldLoad head = new FieldLoad();

    private FieldLoad previousCounter = null;

    private long count = Long.MIN_VALUE;

    static {
        System.out.println("org/conetex/contract/runtime/instrument/counter " + FieldLoad.class + " loaded. count: '" + head.count + "'");
    }

    private FieldLoad(){}

    public static synchronized void increment() {
        if (isInProgress) {
            // We are already inside increment() → endless recursion detected
            return;
        }
        isInProgress = true;
        try {
            if (head.count == Long.MAX_VALUE) {
                FieldLoad newCounter = new FieldLoad();
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

    public static synchronized FieldLoad getHead() {
        return head;
    }

    public FieldLoad getPrevious() {
        return this.previousCounter;
    }

    public long getCount() {
        return this.count;
    }

}

