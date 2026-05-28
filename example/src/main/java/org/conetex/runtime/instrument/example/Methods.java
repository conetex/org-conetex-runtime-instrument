package org.conetex.runtime.instrument.example;

import org.conetex.runtime.instrument.metrics.cost.Counters;

import java.util.TreeMap;

public class Methods {

	public static void foo() {
        System.out.println("foo 2");
    }

	public static void bar() {
    	System.out.println("Example Counter b: ".concat( Long.valueOf(Counters.METHOD_ENTRY.peek().getValue()).toString() ));
        System.out.println("bar 2");
        TreeMap<String, String> x = new TreeMap<>();
    	System.out.println("Example Counter c: ".concat( Long.valueOf(Counters.METHOD_ENTRY.peek().getValue()).toString() ));
    	//x.put("x", "xx");
    	System.out.println("Example Counter c: ".concat( Long.valueOf(Counters.METHOD_ENTRY.peek().getValue()).toString() ));
    	x.put("x", "xx");
    	System.out.println("Example Counter c: ".concat( Long.valueOf(Counters.METHOD_ENTRY.peek().getValue()).toString() ));
    	x.put("x", "xx");
        String test = x.get("x");
        System.out.println( test );
    	System.out.println("Example Counter c: ".concat( Long.valueOf(Counters.METHOD_ENTRY.peek().getValue()).toString() ));

    	System.out.println("Example Counter d: ".concat( Long.valueOf(Counters.METHOD_ENTRY.peek().getValue()).toString() ));

    }
	
}
