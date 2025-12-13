package org.conetex.contract.runtime.instrument.counter;

public class MethodCall implements Counter {

    private static boolean isInProgress = false;

    private static MethodCall head = new MethodCall();

    private MethodCall previousCounter = null;

    private long count = Long.MIN_VALUE;

    static {
        System.out.println("org/conetex/contract/runtime/instrument/counter " + MethodCall.class + " loaded. count: '" + head.count + "'");
    }

    private MethodCall(){}

    public static synchronized void increment() {
        if (isInProgress) {
            // We are already inside increment() → endless recursion detected
            return;
        }
        isInProgress = true;
        try {
            if (head.count == Long.MAX_VALUE) {
                MethodCall newCounter = new MethodCall();
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

    public static synchronized MethodCall getHead() {
        return head;
    }

    public MethodCall getPrevious() {
        return this.previousCounter;
    }

    public long getCount() {
        return this.count;
    }

}

