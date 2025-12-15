package org.conetex.contract.runtime.instrument.counter;

import org.conetex.contract.runtime.instrument.interfaces.Counter;

public class VariableLoad implements Counter {

    private static boolean isInProgress = false;

    private static VariableLoad head = new VariableLoad();

    private VariableLoad previousCounter = null;

    private long count = Long.MIN_VALUE;

    static {
        System.out.println("org/conetex/contract/runtime/instrument/counter " + VariableLoad.class + " loaded. count: '" + head.count + "'");
    }

    private VariableLoad(){}

    public static synchronized void increment() {
        if (isInProgress) {
            // We are already inside increment() → endless recursion detected
            return;
        }
        isInProgress = true;
        try {
            if (head.count == Long.MAX_VALUE) {
                VariableLoad newCounter = new VariableLoad();
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

    public static synchronized VariableLoad getHead() {
        return head;
    }

    public VariableLoad getPrevious() {
        return this.previousCounter;
    }

    public long getCount() {
        return this.count;
    }

}

