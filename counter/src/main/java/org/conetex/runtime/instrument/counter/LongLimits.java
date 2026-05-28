package org.conetex.runtime.instrument.counter;

import org.conetex.runtime.instrument.interfaces.counter.LongLimitsConfiguration;

public record LongLimits(long min, long max) implements LongLimitsConfiguration {}
