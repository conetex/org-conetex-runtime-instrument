package org.conetex.runtime.instrument.test.jar.module;

import java.io.File;

/*
java --module-path "agent/target/agent-0.0.1-SNAPSHOT.jar;interfaces/target/classes;counter/target/classes;metrics-cost/target/classes;test/jar-module/target/classes;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm\9.8\asm-9.8.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm-commons\9.8\asm-commons-9.8.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm-tree\9.8\asm-tree-9.8.jar" --add-modules org.conetex.runtime.instrument.agent,org.conetex.runtime.instrument.metrics.cost,org.conetex.runtime.instrument.test.jar.module -javaagent:agent/target/agent-0.0.1-SNAPSHOT.jar=pathToTransformerJar:../../metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT-fat.jar,../../metrics-cost-unnamed/target/metrics-cost-unnamed-0.0.1-SNAPSHOT.jar -m org.conetex.runtime.instrument.test.jar.module/org.conetex.runtime.instrument.test.jar.module.Main
*/

public class Main {

    public static final String TEST_FAILED = "test FAILED";
    public static final String TEST_OK = "test OK";

    public static void main(String[] args) {
        System.out.println("org.conetex.runtime.instrument.test.jar.module MainModule working here: " + new File(".").getAbsolutePath());
    }

}