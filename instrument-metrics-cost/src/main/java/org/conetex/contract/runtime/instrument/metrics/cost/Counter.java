package org.conetex.contract.runtime.instrument.metrics.cost;

public class Counter {
	static {
		System.out.println("new counter/Counter lives a " + Long.MIN_VALUE);
	}
	public static long count = Long.MIN_VALUE;
	static {
		System.out.println("new counter/Counter lives b " + count);
	}
	
}
