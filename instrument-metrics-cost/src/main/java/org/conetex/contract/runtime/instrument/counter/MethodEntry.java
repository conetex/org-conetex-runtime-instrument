package org.conetex.contract.runtime.instrument.counter;

import org.conetex.contract.runtime.instrument.interfaces.Counter;

public class MethodEntry implements Counter {

    private static boolean isInProgress = false;

    private static MethodEntry head = new MethodEntry();

    private MethodEntry previousCounter = null;

    private long count = Long.MIN_VALUE;

    static {
        System.out.println("org/conetex/contract/runtime/instrument/counter " + MethodEntry.class + " loaded. count: '" + head.count + "'");
    }

    private MethodEntry(){}

    public static synchronized void increment() {
        if (isInProgress) {
            // We are already inside increment() → endless recursion detected
            return;
        }
        isInProgress = true;
        try {
            if (head.count == Long.MAX_VALUE) {
                MethodEntry newCounter = new MethodEntry();
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

    public static synchronized MethodEntry getHead() {
        return head;
    }

    public MethodEntry getPrevious() {
        return this.previousCounter;
    }

    public long getCount() {
        return this.count;
    }

}

