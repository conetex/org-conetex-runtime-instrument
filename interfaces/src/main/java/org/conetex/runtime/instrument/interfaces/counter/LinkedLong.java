package org.conetex.runtime.instrument.interfaces.counter;

/**
 * Represents a counter used to track and measure program-related metrics,
 * such as specific operations or field accesses. Implementations of this interface are designed
 * to be utilized in instrumented environments while ensuring thread safety and avoiding circular
 * calls or interference caused by other instrumented classes.
 * For details regarding the implementation of a typical counter's `increment` method,
 * refer 'org.conetex.contract.runtime.instrument.counter.ArithmeticAddSubNeg'
 * Javadoc description.
 */
public interface LinkedLong {

    boolean hasPrevious();

    LinkedLong getPrevious();

    long getValue();

}
