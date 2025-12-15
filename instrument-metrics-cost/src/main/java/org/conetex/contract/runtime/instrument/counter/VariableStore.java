package org.conetex.contract.runtime.instrument.counter;

import org.conetex.contract.runtime.instrument.interfaces.Counter;

public class VariableStore implements Counter {

    private static boolean isInProgress = false;

    private static VariableStore head = new VariableStore();

    private VariableStore previousCounter = null;

    private long count = Long.MIN_VALUE;

    static {
        System.out.println("org/conetex/contract/runtime/instrument/counter " + VariableStore.class + " loaded. count: '" + head.count + "'");
    }

    private VariableStore(){}

    public static synchronized void increment() {
        if (isInProgress) {
            // We are already inside increment() → endless recursion detected
            return;
        }
        isInProgress = true;
        try {
            if (head.count == Long.MAX_VALUE) {
                VariableStore newCounter = new VariableStore();
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

    public static synchronized VariableStore getHead() {
        return head;
    }

    public VariableStore getPrevious() {
        return this.previousCounter;
    }

    public long getCount() {
        return this.count;
    }

}

