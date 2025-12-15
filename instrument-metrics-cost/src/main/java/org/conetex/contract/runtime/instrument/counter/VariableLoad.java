package org.conetex.contract.runtime.instrument.counter;

import org.conetex.contract.runtime.instrument.interfaces.Counter;

public class VariableLoad extends AbstractCounter {

    private static boolean isInProgress = false;

    private static VariableLoad head = new VariableLoad();

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
            if (head.count == MAX_VALUE) {
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
        head.count = MIN_VALUE;
        head.previousCounter = null;
    }

    public static synchronized VariableLoad getHead() {
        return head;
    }

}

