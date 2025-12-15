package org.conetex.contract.runtime;

import java.lang.instrument.Instrumentation;

public class Agent {

    public static void agentmain(String agentArgs, Instrumentation inst) {
        Instrument.apply(agentArgs, inst);
    }

    // USAGE: -javaagent:agent/target/agent-0.0.1-SNAPSHOT.jar=pathToTransformerJar:../../instrument-metrics-cost/target/instrument-metrics-cost-0.0.1-SNAPSHOT.jar
    public static void premain(String agentArgs, Instrumentation inst) {
        Instrument.apply(agentArgs, inst);
    }

}