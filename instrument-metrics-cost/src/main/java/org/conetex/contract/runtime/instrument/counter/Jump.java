package org.conetex.contract.runtime.instrument.counter;

import org.conetex.contract.runtime.instrument.interfaces.Counter;

public class Jump extends AbstractCounter {

    private static boolean isInProgress = false;

    private static Jump head = new Jump();

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
            if (head.count == MAX_VALUE) {
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
        head.count = MIN_VALUE;
        head.previousCounter = null;
    }

    public static synchronized Jump getHead() {
        return head;
    }

}

