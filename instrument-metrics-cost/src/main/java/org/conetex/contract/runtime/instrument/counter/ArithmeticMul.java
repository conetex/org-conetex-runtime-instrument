package org.conetex.contract.runtime.instrument.counter;

public class ArithmeticMul implements Counter {

    private static boolean isInProgress = false;

    private static ArithmeticMul head = new ArithmeticMul();

    private ArithmeticMul previousCounter = null;

    private long count = Long.MIN_VALUE;

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
            if (head.count == Long.MAX_VALUE) {
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
        head.count = Long.MAX_VALUE;
        head.previousCounter = null;
    }

    public static synchronized ArithmeticMul getHead() {
        return head;
    }

    public ArithmeticMul getPrevious() {
        return this.previousCounter;
    }

    public long getCount() {
        return this.count;
    }

}

