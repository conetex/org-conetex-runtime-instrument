package org.conetex.runtime.instrument.agent;

import java.lang.instrument.Instrumentation;

public class Agent {

    public static void agentmain(String agentArgs, Instrumentation inst) {
        Instrument.apply(agentArgs, inst);
    }

    // USAGE
    // cp mode:
    // java -javaagent:<-PATH_TO_AGENT_JAR--------------------->=pathToTransformerJar:<-PATH_TO_TRANSFORMER---RELATIVE_TO_AGENT-------------------> -cp <-PATH_TO_INSTRUMENTED_JAR-----------> <-MAIN_CLASS_OF_INSTRUMENTED_JAR----------->
    // java -javaagent:agent/target/agent-0.0.1-SNAPSHOT-fat.jar=pathToTransformerJar:../../metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT-fat.jar -cp test/jar/target/jar-0.0.1-SNAPSHOT.jar org.conetex.runtime.instrument.test.jar.Main
    // you can add -Xbootclasspath/a:metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT-fat.jar
    // java -Xbootclasspath/a:metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT-fat.jar -javaagent:agent/target/agent-0.0.1-SNAPSHOT-fat.jar=pathToTransformerJar:../../metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT-fat.jar -cp test/jar/target/jar-0.0.1-SNAPSHOT.jar org.conetex.runtime.instrument.test.jar.Main
    /* module mode:
    java --module-path "agent/target/agent-0.0.1-SNAPSHOT.jar;interfaces/target/classes;counter/target/classes;metrics-cost/target/classes;test/jar-module/target/classes;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm\9.8\asm-9.8.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm-commons\9.8\asm-commons-9.8.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm-tree\9.8\asm-tree-9.8.jar" --add-modules org.conetex.runtime.instrument.agent,org.conetex.runtime.instrument.metrics.cost,org.conetex.runtime.instrument.test.jar.module -javaagent:agent/target/agent-0.0.1-SNAPSHOT.jar=pathToTransformerJar:../../metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT-fat.jar -m org.conetex.runtime.instrument.test.jar.module/org.conetex.runtime.instrument.test.jar.module.Main
     */


    public static void premain(String agentArgs, Instrumentation inst) {
        Instrument.apply(agentArgs, inst);
    }

}