package org.conetex.contract.runtime.instrument.counter;

public class Jump implements Counter {

    private static boolean isInProgress = false;

    private static Jump head = new Jump();

    private Jump previousCounter = null;

    private long count = Long.MIN_VALUE;

    static {
        System.out.println("org/conetex/contract/runtime/instrument/counter " + Jump.class + " loaded. count: '" + head.count + "'");
    }

    private Jump(){}

    public static synchronized void increment() {
        if (isInProgress) {
            // We are already inside increment() → endless recursion detected
            return;
        }
        isInProgress = true;
        try {
            if (head.count == Long.MAX_VALUE) {
                Jump newCounter = new Jump();
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

    public static synchronized Jump getHead() {
        return head;
    }

    public Jump getPrevious() {
        return this.previousCounter;
    }

    public long getCount() {
        return this.count;
    }

}

