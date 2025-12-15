package org.conetex.contract.runtime.instrument.counter;

import org.conetex.contract.runtime.instrument.interfaces.Counter;

public class FieldStore implements Counter {

    private static boolean isInProgress = false;

    private static FieldStore head = new FieldStore();

    private FieldStore previousCounter = null;

    private long count = Long.MIN_VALUE;

    static {
        System.out.println("org/conetex/contract/runtime/instrument/counter " + FieldStore.class + " loaded. count: '" + head.count + "'");
    }

    private FieldStore(){}

    public static synchronized void increment() {
        if (isInProgress) {
            // We are already inside increment() → endless recursion detected
            return;
        }
        isInProgress = true;
        try {
            if (head.count == Long.MAX_VALUE) {
                FieldStore newCounter = new FieldStore();
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

    public static synchronized FieldStore getHead() {
        return head;
    }

    public FieldStore getPrevious() {
        return this.previousCounter;
    }

    public long getCount() {
        return this.count;
    }

}

