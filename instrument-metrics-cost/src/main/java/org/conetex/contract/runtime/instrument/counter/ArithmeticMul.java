package org.conetex.contract.runtime.instrument.counter;

import org.conetex.contract.runtime.instrument.interfaces.Counter;

public class ArithmeticMul extends AbstractCounter {

    private static boolean isInProgress = false;

    private static ArithmeticMul head = new ArithmeticMul();

    static {
        System.out.println("org/conetex/contract/runtime/instrument/counter " + ArithmeticMul.class + " loaded. count: '" + head.count + "'");
    }

    private ArithmeticMul(){}

    public static synchronized void increment() {
        if (isInProgress) {
            // We are already inside increment() → endless recursion detected
            return;
        }
        isInProgress = true;
        try {
            if (head.count == MAX_VALUE) {
                ArithmeticMul newCounter = new ArithmeticMul();
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

    public static synchronized ArithmeticMul getHead() {
        return head;
    }

}

