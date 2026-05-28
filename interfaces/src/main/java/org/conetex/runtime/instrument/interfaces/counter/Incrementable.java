package org.conetex.runtime.instrument.interfaces.counter;

public interface Incrementable {

    void blockIncrement(boolean incrementationBlocked);

    LinkedLong peek();

    void reset();

    void increment();

}
