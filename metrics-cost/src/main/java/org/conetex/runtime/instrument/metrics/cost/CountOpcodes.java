package org.conetex.runtime.instrument.metrics.cost;

import org.conetex.runtime.instrument.collection.AvlTree;
import org.conetex.runtime.instrument.counter.Counter;
import org.conetex.runtime.instrument.counter.LongLimits;

public class CountOpcodes {

    public static final AvlTree.Map<String, Counter> COUNTERS = new AvlTree.Map<>();

    private final static LongLimits CONFIG_MIN_MAX = new LongLimits(0L, Long.MAX_VALUE);

    public static void consume(String counterName, int opcode) {
        //System.out.println("consumed " + counterName + " " + opcode);
        COUNTERS.insertIntoTree(
                counterName + ";" + opcode,
                ()->{
                    Counter newCounter = new Counter(CONFIG_MIN_MAX, false);
                    newCounter.increment();
                    return newCounter;
                },
                (Counter existingCounter)->{
                    existingCounter.increment();
                    return existingCounter;
                }
        );
        COUNTERS.findInTree(counterName + ";" + opcode);
    }

    public static void reset() {
        for(AvlTree.Entry<String, Counter> counterOfOpcode : COUNTERS){
            counterOfOpcode.value().reset();
        }
    }

    public static void blockIncrement(boolean incrementationBlocked) {
        for(AvlTree.Entry<String, Counter> counterOfOpcode : COUNTERS){
            counterOfOpcode.value().blockIncrement(incrementationBlocked);
        }
    }

}
