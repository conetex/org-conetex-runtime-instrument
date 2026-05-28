package org.conetex.runtime.instrument.test.jar;

import org.conetex.runtime.instrument.counter.Counter;
import org.conetex.runtime.instrument.counter.CountersWeighted;
import org.conetex.runtime.instrument.interfaces.arithmetic.ChainsOfLongs;
import org.conetex.runtime.instrument.interfaces.counter.Incrementable;
import org.conetex.runtime.instrument.metrics.cost.Counters;

import java.io.File;
/*
java -javaagent:agent/target/agent-0.0.1-SNAPSHOT.jar=pathToTransformerJar:../../metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT-fat.jar,../../metrics-cost-unnamed/target/metrics-cost-unnamed-0.0.1-SNAPSHOT.jar -cp test/jar/target/jar-0.0.1-SNAPSHOT.jar org.conetex.runtime.instrument.test.jar.Main
 */
public class MainJar {

    public static final String TEST_FAILED = "test FAILED";
    public static final String TEST_OK = "test OK";

    // TODO for whatever reason tests are not successfully for every run. threads? duplicate counters?
    public static void main(String[] args) {

        System.out.println("org.conetex.runtime.instrument.test.jar MainTest working here: " + new File(".").getAbsolutePath());

    }


}