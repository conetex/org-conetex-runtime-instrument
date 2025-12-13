package org.conetex.contract.runtime.instrument.counter;

public interface Counter {

    public Counter getPrevious();

    public long getCount();

}
