package org.conetex.contract.runtime.instrument.counter;

import org.conetex.contract.runtime.instrument.interfaces.Counter;

public class VariableStore extends AbstractCounter {

    private static boolean isInProgress = false;

    private static VariableStore head = new VariableStore();

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
            if (head.count == MAX_VALUE) {
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
        head.count = MIN_VALUE;
        head.previousCounter = null;
    }

    public static synchronized VariableStore getHead() {
        return head;
    }


}

