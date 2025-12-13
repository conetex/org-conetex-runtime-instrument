package org.conetex.contract.runtime.instrument.counter;

public class ArithmeticAddSubNeg implements Counter {

    private static boolean isInProgress = false;

    private static ArithmeticAddSubNeg head = new ArithmeticAddSubNeg();

    private ArithmeticAddSubNeg previousCounter = null;

    private long count = Long.MIN_VALUE;

    static {
        System.out.println("org/conetex/contract/runtime/instrument/counter " + ArithmeticAddSubNeg.class + " loaded. count: '" + head.count + "'");
    }

    private ArithmeticAddSubNeg(){}

    public static synchronized void increment() {
        if (isInProgress) {
            // We are already inside increment() → endless recursion detected
            return;
        }
        isInProgress = true;
        try {
            if (head.count == Long.MAX_VALUE) {
                ArithmeticAddSubNeg newCounter = new ArithmeticAddSubNeg();
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

    public static synchronized ArithmeticAddSubNeg getHead() {
        return head;
    }

    public ArithmeticAddSubNeg getPrevious() {
        return this.previousCounter;
    }

    public long getCount() {
        return this.count;
    }

}

