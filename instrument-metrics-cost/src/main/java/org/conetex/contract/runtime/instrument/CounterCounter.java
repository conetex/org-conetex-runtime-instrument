package org.conetex.contract.runtime.instrument;

import org.conetex.contract.runtime.instrument.counter.AbstractCounter;
import org.conetex.contract.runtime.instrument.interfaces.Counter;

public class CounterCounter extends AbstractCounter {

    private boolean isInProgress = false;

    CounterCounter(){}

    public synchronized CounterCounter increment() {
        if (isInProgress) {
            // We are already inside increment() → endless recursion detected
            return this;
        }
        isInProgress = true;
        try {
            if (super.count == MAX_VALUE) {
                CounterCounter newCounter = new CounterCounter();
                newCounter.previousCounter = this;
                return newCounter;
            } else {
                this.count++;
                return this;
            }
        } finally {
            isInProgress = false;
        }
    }

    static synchronized CounterCounter[] countCounters(Counter[] counters) {
        CounterCounter[] counterCounter = new CounterCounter[counters.length];
        boolean empty = true;
        for (int i = 0; i < counters.length; i++) {
            Counter current;
            if(counters[i] != null && (current = counters[i].getPrevious()) != null){
                counterCounter[i] = new CounterCounter();
                empty = false;
                while (current != null) {

                    if (counterCounter[i].count == MAX_VALUE) {
                        CounterCounter newCounter = new CounterCounter();
                        newCounter.previousCounter = counterCounter[i];
                        counterCounter[i] = newCounter;
                    } else {
                        counterCounter[i].count++;
                    }

                    current = current.getPrevious();
                }
            }
        }
        if(empty){
            return null;
        }
        return counterCounter;
    }

}

