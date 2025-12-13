package org.conetex.contract.runtime.instrument.counter;

public class ArithmeticDivRem implements Counter {

    private static boolean isInProgress = false;

    private static ArithmeticDivRem head = new ArithmeticDivRem();

    private ArithmeticDivRem previousCounter = null;

    private long count = Long.MIN_VALUE;

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
            if (head.count == Long.MAX_VALUE) {
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
        head.count = Long.MAX_VALUE;
        head.previousCounter = null;
    }

    public static synchronized ArithmeticDivRem getHead() {
        return head;
    }

    public ArithmeticDivRem getPrevious() {
        return this.previousCounter;
    }

    public long getCount() {
        return this.count;
    }

}

