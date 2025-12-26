package org.conetex.runtime.instrument.counter;

import org.conetex.runtime.instrument.interfaces.counter.LinkedLong;
import org.conetex.runtime.instrument.interfaces.counter.LongLimitsConfiguration;

final class Node implements LinkedLong {

    final LinkedLong previousCounter;

    long value;

    Node(LinkedLong previous, LongLimitsConfiguration config){
        this.previousCounter = previous;
        this.value = config.min();
    }

    Node(Node original){
        this.previousCounter = original.previousCounter;
        this.value = original.value;
    }

    public long getValue() {
        return this.value;
    }

    public LinkedLong getPrevious() {
        return this.previousCounter;
    }

    public boolean hasPrevious() {
        return true;
    }

}

