package org.conetex.runtime.instrument.example;



import org.conetex.runtime.instrument.counter.Counter;
import org.conetex.runtime.instrument.counter.CountersWeighted;
import org.conetex.runtime.instrument.example.subpackage.ClassFromOtherPackage;
import org.conetex.runtime.instrument.metrics.cost.Counters;

import java.io.File;
import java.util.Arrays;
import java.util.TreeMap;

public class Main {

    // -javaagent:/agent/target/agent-0.0.0-SNAPSHOT.jar=pathToTransformerJar:../../metrics-cost/target/metrics-cost-0.0.0-SNAPSHOT.jar
    public static void main(String[] args) {

        //Counters.blockIncrement(false);

        System.out.println( "A: " + Arrays.toString(Counters.CONFIG.average()) );
        //Counters.reset();
        //System.out.println( "B: " + Arrays.toString(Counters.CONFIG.average()) );

        System.out.println("working here: " + new File(".").getAbsolutePath());
        System.out.println("S T A R T");
        System.out.println("Example Counter at start: " + Counters.METHOD_ENTRY.peek().getValue() + " ");
        System.out.println("Example Jumps at start: " + Counters.JUMP.peek().getValue() + " ");

        System.out.println( "B: " + Arrays.toString(Counters.CONFIG.average()) );
        //MethodCalls.head.count = Long.MIN_VALUE;
        System.out.println("Example Counter at reset: " + Counters.METHOD_ENTRY.peek().getValue() + " ");
        System.out.println("Example Jumps at reset: " + Counters.JUMP.peek().getValue() + " ");

        System.out.println("Example Counter before real nothing: " + Counters.METHOD_ENTRY.peek().getValue() + " ");
        System.out.println("Example Jumps before real nothing: " + Counters.JUMP.peek().getValue() + " ");
        System.out.println("Example Jumps after real nothing: " + Counters.JUMP.peek().getValue() + " ");
        System.out.println("Example Counter after real nothing: " + Counters.METHOD_ENTRY.peek().getValue() + " ");

        System.out.println("Example Counter before nothing: " + Counters.METHOD_ENTRY.peek().getValue() + " ");
        System.out.println("Example Jumps before nothing: " + Counters.JUMP.peek().getValue() + " ");
        nothing();
        System.out.println("Example Jumps after nothing: " + Counters.JUMP.peek().getValue() + " ");
        System.out.println("Example Counter after nothing: " + Counters.METHOD_ENTRY.peek().getValue() + " ");

        long ix = Counters.METHOD_ENTRY.peek().getValue();
        System.out.println("loaded counter..." + ix);
        long xx = Long.MAX_VALUE;
        String xxx = Long.toString(xx);
        System.out.println("Example Jumps at toString: " + Counters.JUMP.peek().getValue() + " ");
        System.out.println("Example Counter: " + Counters.METHOD_ENTRY.peek().getValue() + " " + xxx);
        ix = Counters.METHOD_ENTRY.peek().getValue();
        System.out.println("loaded counter..." + ix);
        System.out.println("load counter..." + xx);
        System.out.println("Example Counter: ".concat(Long.toString(Counters.METHOD_ENTRY.peek().getValue())) );
        ClassFromOtherPackage.test();
        ix = Counters.METHOD_ENTRY.peek().getValue();
        System.out.println("loaded counter..." + ix);
        foo();
        System.out.println("Example Counter: " + Counters.METHOD_ENTRY.peek().getValue());
        Methods.foo();
        System.out.println("Example Counter: " + Counters.METHOD_ENTRY.peek().getValue());
        Methods.bar();
        System.out.println("Example Counter: " + Counters.METHOD_ENTRY.peek().getValue());
        bar();
        System.out.println("Example Counter: " + Counters.METHOD_ENTRY.peek().getValue());

        TreeMap<String, String> treeX = new TreeMap<>();
        System.out.println("Example Counter x: ".concat( Long.valueOf(Counters.METHOD_ENTRY.peek().getValue()).toString() ));
        treeX.put("x", "xx");

        String isInMap = treeX.get("x");
        System.out.println("loaded counter..." + isInMap);
        System.out.println("Example Counter x: ".concat( Long.valueOf(Counters.METHOD_ENTRY.peek().getValue()).toString() ));
        System.out.println("Example Counter x: ".concat( Long.valueOf(Counters.METHOD_ENTRY.peek().getValue()).toString() ));

        /*for(int i = 0; i < Counters.COUNTERS.length; i++){
            long i1 = Counters.COUNTERS[i].peek().getValue();
            Counters.CONFIG.average();
            long i2 = Counters.COUNTERS[i].peek().getValue();
            System.out.println("test call average 2 effects " + i + ": " + i2 + " - " + i1 + " = " + (i2-i1));
        }*/



        System.out.println( "C: " + Arrays.toString(Counters.CONFIG.average()) );
    }



    static void nothing() {
    }

    static void foo() {
        System.out.println("foo");
    }

    static void bar() {
        System.out.println("bar");
    }

}