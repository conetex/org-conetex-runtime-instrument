package org.conetex.contract.runtime.instrument.counter;

import org.conetex.contract.runtime.instrument.interfaces.Counter;

public abstract class AbstractCounter implements Counter {

    public final static long MAX_VALUE = Long.MAX_VALUE;

    public final static long MIN_VALUE = Long.MIN_VALUE;

    protected long count = MIN_VALUE;

    protected AbstractCounter previousCounter = null;

    public final long getCount() {
        return this.count;
    }

    public final Counter getPrevious() {
        return this.previousCounter;
    }

}

