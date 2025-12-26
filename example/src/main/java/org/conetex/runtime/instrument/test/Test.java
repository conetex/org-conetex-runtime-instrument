package org.conetex.runtime.instrument.test;

import org.conetex.runtime.instrument.counter.Counter;
import org.conetex.runtime.instrument.counter.CountersWeighted;
import org.conetex.runtime.instrument.metrics.cost.Counters;

public class Test {

    // java -javaagent:agent/target/agent-0.0.1-SNAPSHOT-fat.jar=pathToTransformerJar:../../metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT-fat.jar -cp example/target/example-0.0.1-SNAPSHOT.jar org.conetex.runtime.instrument.test.Test
    public static void main(String[] args) {
        // CountersWeighted (ChainsOfLongs)
        for(int i = 0; i < Counters.COUNTERS.length; i++){
            CountersWeighted cs = Counters.CONFIG;
            Counter c = Counters.COUNTERS[i];
            long i1 = c.peek().getValue();
            Counters.CONFIG.average();
            long i2 = c.peek().getValue();
            System.out.println("test call ChainsOfLongs.average effects " + i + " (expected result is 0): " + i2 + " - " + i1 + " = " + (i2-i1)); // ok
        }

        // Counter (Incrementable)
        for(int i = 0; i < Counters.COUNTERS.length; i++){
            CountersWeighted cs = Counters.CONFIG;
            Counter c = Counters.COUNTERS[i];
            c.reset();
            long i1 = c.peek().getValue();
            c.blockIncrement(true);
            c.increment();
            c.blockIncrement(false);
            long i2 = c.peek().getValue();
            System.out.println("test call Incrementable.* effects " + i + " (expected result is 0): " + i2 + " - " + i1 + " = " + (i2-i1)); // ok
        }

        for(int i = 0; i < Counters.COUNTERS.length; i++){
            CountersWeighted cs = Counters.CONFIG;
            Counter c = Counters.COUNTERS[i];
            c.reset();
            long i1 = c.peek().getValue();
            c.increment();
            long i2 = c.peek().getValue();
            System.out.println("test call Incrementable.* effects " + i + " (expected result is 1): " + i2 + " - " + i1 + " = " + (i2-i1)); // ok
        }

        // Counters
        for(int i = 0; i < Counters.COUNTERS.length; i++){
            CountersWeighted cs = Counters.CONFIG;
            Counter c = Counters.COUNTERS[i];
            Counters.reset();
            long i1 = c.peek().getValue();
            Counters.reset();
            long i2 = c.peek().getValue();
            System.out.println("test call reset Counters.reset effects " + i + " (expected result is 0): " + i2 + " - " + i1 + " = " + (i2-i1)); // ok
        }

        // Counters
        for(int i = 0; i < Counters.COUNTERS.length; i++){
            long i1 = Counters.COUNTERS[i].peek().getValue();
            Counters.blockIncrement(true);
            Counters.blockIncrement(true);
            Counters.blockIncrement(false);
            Counters.blockIncrement(false);
            Counters.blockIncrement(true);
            Counters.blockIncrement(true);
            Counters.blockIncrement(false);
            Counters.blockIncrement(false);
            long i2 = Counters.COUNTERS[i].peek().getValue();
            System.out.println("test call Counters.blockIncrement effects " + i + " (expected result is 0): " + i2 + " - " + i1 + " = " + (i2-i1)); // ok
        }
    }

}