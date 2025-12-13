package org.conetex.contract.runtime.instrument.counter;

public class TypeCheck implements Counter {

    private static boolean isInProgress = false;

    private static TypeCheck head = new TypeCheck();

    private TypeCheck previousCounter = null;

    private long count = Long.MIN_VALUE;

    static {
        System.out.println("org/conetex/contract/runtime/instrument/counter " + TypeCheck.class + " loaded. count: '" + head.count + "'");
    }

    private TypeCheck(){}

    public static synchronized void increment() {
        if (isInProgress) {
            // We are already inside increment() → endless recursion detected
            return;
        }
        isInProgress = true;
        try {
            if (head.count == Long.MAX_VALUE) {
                TypeCheck newCounter = new TypeCheck();
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

    public static synchronized TypeCheck getHead() {
        return head;
    }

    public TypeCheck getPrevious() {
        return this.previousCounter;
    }

    public long getCount() {
        return this.count;
    }

}

