package org.conetex.contract.runtime.instrument.counter;

import org.conetex.contract.runtime.instrument.interfaces.Counter;

public class ArithmeticDivRem extends AbstractCounter {

    private static boolean isInProgress = false;

    private static ArithmeticDivRem head = new ArithmeticDivRem();

    static {
        System.out.println("org/conetex/contract/runtime/instrument/counter " + ArithmeticDivRem.class + " loaded. count: '" + head.count + "'");
    }

    private ArithmeticDivRem(){}

    public static synchronized void increment() {
        if (isInProgress) {
            // We are already inside increment() → endless recursion detected
            return;
        }
        isInProgress = true;
        try {
            if (head.count == MAX_VALUE) {
                ArithmeticDivRem newCounter = new ArithmeticDivRem();
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

    public static synchronized ArithmeticDivRem getHead() {
        return head;
    }

}

