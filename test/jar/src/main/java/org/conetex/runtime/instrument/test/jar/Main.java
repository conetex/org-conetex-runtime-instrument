package org.conetex.runtime.instrument.test.jar;

import org.conetex.runtime.instrument.counter.Counter;
import org.conetex.runtime.instrument.counter.CountersWeighted;
import org.conetex.runtime.instrument.interfaces.arithmetic.ChainsOfLongs;
import org.conetex.runtime.instrument.interfaces.counter.Incrementable;
import org.conetex.runtime.instrument.metrics.cost.Counters;

public class Main {

    public static final String TEST_FAILED = "test failed";
    public static final String TEST_OK = "test ok";

    public static void main(String[] args) {
        // warmup 1
        for (int i = 0; i < Counters.COUNTERS.length; i++) {
            Incrementable c = Counters.COUNTERS[i];
            c.reset();
            Counters.COUNTERS[i].reset();
            long counterBefore = c.peek().getValue();
            c.increment();
            long counterAfter = c.peek().getValue();
            //outputResult(counterAfter, counterBefore, expected, testName, i);
        }

        // warmup 2
        Counters.CONFIG.average();

        // test
        testsIncrementableInterfaceDefault();
        testsIncrementableInterfaceBlock();
        testsIncrementableCounterDefault();
        testsIncrementableCounterBlock();
        testsChainsOfLongsCountersWeighted();
        testsChainsOfLongsInterface();
        testsCountersReset();
        testCountersBlockIncrement();
    }

    public static void testsIncrementableCounterDefault() {
        String testName = "Counter (Incrementable) | default increment";
        long expected = 1;

        for(int i = 0; i < Counters.COUNTERS.length; i++){
            Counter c = Counters.COUNTERS[i];
            c.reset();
            long counterBefore = c.peek().getValue();
            c.increment();
            long counterAfter = c.peek().getValue();

            outputResult(counterAfter, counterBefore, expected, testName, i);

        }
    }

    public static void testsIncrementableCounterBlock() {
        String testName = "Counter (Incrementable) | blocked increment";
        long expected = 0;

        for(int i = 0; i < Counters.COUNTERS.length; i++){
            Counter c = Counters.COUNTERS[i];
            c.reset();
            long counterBefore = c.peek().getValue();
            c.blockIncrement(true);
            c.increment();
            c.blockIncrement(false);
            long counterAfter = c.peek().getValue();

            outputResult(counterAfter, counterBefore, expected, testName, i);

        }
    }

    public static void testsIncrementableInterfaceDefault() {
        String testName = "Incrementable | default increment";
        long expected = 1;

        for (int i = 0; i < Counters.COUNTERS.length; i++) {
            Incrementable c = Counters.COUNTERS[i];
            c.reset();
            Counters.COUNTERS[i].reset();
            long counterBefore = c.peek().getValue();
            c.increment();
            long counterAfter = c.peek().getValue();

            outputResult(counterAfter, counterBefore, expected, testName, i);

        }

    }

    public static void testsIncrementableInterfaceBlock() {
        String testName = "Incrementable | blocked increment";
        long expected = 0;

        for(int i = 0; i < Counters.COUNTERS.length; i++){
            Incrementable c = Counters.COUNTERS[i];
            c.reset();
            long counterBefore = c.peek().getValue();
            c.blockIncrement(true);
            c.increment();
            c.blockIncrement(false);
            long counterAfter = c.peek().getValue();

            outputResult(counterAfter, counterBefore, expected, testName, i);

        }
    }

    public static void testsChainsOfLongsCountersWeighted() {
        String testName = "CountersWeighted (ChainsOfLongs) | average";
        long expected = 0;

        CountersWeighted testObject = Counters.CONFIG;
        for (int i = 0; i < Counters.COUNTERS.length; i++) {
            Counter c = Counters.COUNTERS[i];

            long counterBefore = c.peek().getValue();
            Counters.CONFIG.average();
            long counterAfter = c.peek().getValue();


            outputResult(counterAfter, counterBefore, expected, testName, i);

        }
    }

    public static void testsChainsOfLongsInterface() {
        String testName = "CountersWeighted (ChainsOfLongs) | average";
        long expected = 0;

        ChainsOfLongs testObject = Counters.CONFIG;
        for (int i = 0; i < Counters.COUNTERS.length; i++) {
            Counter c = Counters.COUNTERS[i];

            long counterBefore = c.peek().getValue();
            testObject.average();
            long counterAfter = c.peek().getValue();


            outputResult(counterAfter, counterBefore, expected, testName, i);

        }
    }

    public static void testsCountersReset() {
        String testName = "Counters | reset";
        long expected = 0;

        for(int i = 0; i < Counters.COUNTERS.length; i++){
            Counter c = Counters.COUNTERS[i];
            Counters.reset();
            long counterBefore = c.peek().getValue();
            Counters.reset();
            long counterAfter = c.peek().getValue();

            outputResult(counterAfter, counterBefore, expected, testName, i);

        }
    }

    public static void testCountersBlockIncrement() {

        String testName = "Counters | blockIncrement";
        long expected = 0;

        for(int i = 0; i < Counters.COUNTERS.length; i++){
            long counterBefore = Counters.COUNTERS[i].peek().getValue();
            Counters.blockIncrement(true);
            Counters.blockIncrement(true);
            Counters.blockIncrement(false);
            Counters.blockIncrement(false);
            Counters.blockIncrement(true);
            Counters.blockIncrement(true);
            Counters.blockIncrement(false);
            Counters.blockIncrement(false);
            long counterAfter = Counters.COUNTERS[i].peek().getValue();

            outputResult(counterAfter, counterBefore, expected, testName, i);

        }

    }

    private static void outputResult(long counterAfter, long counterBefore, long expected, String testName, int i) {
        long counterDiff = (counterAfter - counterBefore);
        if (counterDiff != expected) {
            System.err.println(TEST_FAILED + ": '" + testName + "', on: '" + i + "', expected: '" + expected + "', actual: " + counterAfter + " - " + counterBefore + " = " + counterDiff); // ok
        } else {
            System.out.println(TEST_OK + ": '" + testName + "', on: '" + i + "', expected: '" + expected + "', actual: " + counterAfter + " - " + counterBefore + " = " + counterDiff); // ok
        }
    }

}