package org.conetex.runtime.instrument;

import java.lang.instrument.Instrumentation;

public class Agent {

    public static void agentmain(String agentArgs, Instrumentation inst) {
        Instrument.apply(agentArgs, inst);
    }

    // USAGE:
    // java -javaagent:<-PATH_TO_AGENT_JAR--------------------->=pathToTransformerJar:<-PATH_TO_TRANSFORMER---RELATIVE_TO_AGENT-------------------> -cp <-PATH_TO_INSTRUMENTED_JAR-----------> <-MAIN_CLASS_OF_INSTRUMENTED_JAR----------->
    // java -javaagent:agent/target/agent-0.0.1-SNAPSHOT-fat.jar=pathToTransformerJar:../../metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT-fat.jar -cp test/jar/target/jar-0.0.1-SNAPSHOT.jar org.conetex.runtime.instrument.test.jar.Main
    public static void premain(String agentArgs, Instrumentation inst) {
        Instrument.apply(agentArgs, inst);
    }

}