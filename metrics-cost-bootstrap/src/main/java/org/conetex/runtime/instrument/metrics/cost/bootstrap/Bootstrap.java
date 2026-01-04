package org.conetex.runtime.instrument.metrics.cost.bootstrap;

import java.lang.invoke.*;
import java.util.Arrays;

public class Bootstrap {

    static {
        System.out.println("Bootstrap loaded: " + Bootstrap.class + " (class) - " + Bootstrap.class.getModule() + " (module) - " + Bootstrap.class.getClassLoader() + " (loader)");
        try {
            Class.forName("org.conetex.runtime.instrument.metrics.cost.Counters", true, null);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("can not bootstrap " + e.getMessage());
        }

        try {
            CallSite callToNirvana = bootstrap(MethodHandles.lookup(), "nirvana", MethodType.methodType(void.class), Bootstrap.class);
            callToNirvana.getTarget().invoke();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static int i = 0;
    private static boolean incrementationBlocked = false;
    public static synchronized void nirvana(){
        //System.out.println("daya");
        if (bootstrapInProgress) {
            // We are already inside increment() → endless recursion detected
            return;
        }
        if (incrementationBlocked) {
            // We are already inside increment() → endless recursion detected
            return;
        }
        incrementationBlocked = true;
        try {
            i = i++;
            i = i++;
            //System.out.println("daya " + Arrays.toString(Thread.currentThread().getStackTrace()));
        } finally {
            incrementationBlocked = false;
        }
    }

/*
        name = "nirvana";
        realOwner = Bootstrap.class;

 */
    public static class BootstrapCyclicCallException extends IllegalStateException {
        public BootstrapCyclicCallException(String message) {
            super(message);
        }
    }
    private static boolean bootstrapInProgress = false;
    public static synchronized CallSite bootstrap(MethodHandles.Lookup lookup, String name, MethodType type,
                                     Class<?> realOwner) throws NoSuchMethodException, IllegalAccessException {
        name = "nirvana";
        realOwner = Bootstrap.class;
        if (incrementationBlocked) {
            // We are already inside increment() → endless recursion detected
            return new ConstantCallSite(MethodHandles.empty(type));
        }
        if (bootstrapInProgress) {
            // We are already inside increment() → endless recursion detected
            return new ConstantCallSite(MethodHandles.empty(type));
        }
        bootstrapInProgress = true;
        try {
            Module realOwnerModule = realOwner.getModule();
            if (!realOwnerModule.isExported(realOwner.getPackageName())) {
                throw new NoSuchMethodException(realOwnerModule + " does not export " + realOwner.getPackageName());
            }

            System.out.println("bootstrap: " + realOwner + " | " + name + " | " + type);
            // Link the dynamic method to an actual implementation elsewhere (possibly in another class)
            MethodHandle targetMethodHandle = null;
            try {
                targetMethodHandle = lookup.findStatic(
                        realOwner, // Real owner of the "incrementCompareInt" method
                        name,      // Actual method name
                        type       // Method descriptor
                );
            } catch (IllegalAccessException e) {
                System.err.println("bootstrap IllegalAccessException: " + realOwner + " | " + name + " | " + type + " | " + e.getMessage() + " ");
                throw e;
            } catch (Throwable e) {
                System.err.println("bootstrap Throwable: " + realOwner + " | " + name + " | " + type + " | " + e.getMessage() + " ");
                throw e;
            }

            // Return a CallSite that links to the targetMethodHandle
            return new ConstantCallSite(targetMethodHandle);
        }
        finally {
            bootstrapInProgress = false;
        }
    }

    // todo this leads to crash:


    private static boolean lookupAccessIsAllowed(MethodHandles.Lookup lookup, Class<?> realOwner) {
        // Check if the Lookup has access rights to the realOwner class
        Module ownerModule = realOwner.getModule();
        return ownerModule.isExported(realOwner.getPackageName());
    }
}


/*

-- start fat add   --> load into wrong classloeder Counters loaded 2 times
java --module-path "agent/target/agent-0.0.1-SNAPSHOT.jar;interfaces/target/classes;counter/target/classes;metrics-cost/target/classes;test/jar-module/target/classes;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm\9.8\asm-9.8.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm-commons\9.8\asm-commons-9.8.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm-tree\9.8\asm-tree-9.8.jar" --add-modules org.conetex.runtime.instrument.agent,org.conetex.runtime.instrument.metrics.cost,org.conetex.runtime.instrument.test.jar.module -javaagent:agent/target/agent-0.0.1-SNAPSHOT-fat.jar=pathToTransformerJar:../../metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT-fat.jar -Xbootclasspath/a:metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT-fat.jar -m org.conetex.runtime.instrument.test.jar.module/org.conetex.runtime.instrument.test.jar.module.Main


-Xbootclasspath/a:bootstrap/target/bootstrap-0.0.1-SNAPSHOT.jar;metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT-fat.jar

GO HERE
-- start  add   --> .NoClassDefFoundError:  java.base/java.util.TreeSet.contains(TreeSet.java) org/conetex/runtime/instrument/metrics/cost/Counters
java --module-path "agent/target/agent-0.0.1-SNAPSHOT.jar;interfaces/target/classes;counter/target/classes;metrics-cost/target/classes;test/jar-module/target/classes;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm\9.8\asm-9.8.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm-commons\9.8\asm-commons-9.8.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm-tree\9.8\asm-tree-9.8.jar" --add-modules org.conetex.runtime.instrument.metrics.cost -javaagent:agent/target/agent-0.0.1-SNAPSHOT.jar=pathToTransformerJar:../../metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT-fat.jar -m org.conetex.runtime.instrument.test.jar.module/org.conetex.runtime.instrument.test.jar.module.Main

-- start  add   --> .NoClassDefFoundError:  java.base/java.util.TreeSet.contains(TreeSet.java) org/conetex/runtime/instrument/metrics/cost/Counters
java --module-path "agent/target/agent-0.0.1-SNAPSHOT.jar;interfaces/target/interfaces-0.0.1-SNAPSHOT.jar;counter/target/counter-0.0.1-SNAPSHOT.jar;metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT.jar;test/jar-module/target/jar-module-0.0.1-SNAPSHOT.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm\9.8\asm-9.8.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm-commons\9.8\asm-commons-9.8.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm-tree\9.8\asm-tree-9.8.jar" --patch-module "java.instrument=bootstrap/target/bootstrap-0.0.1-SNAPSHOT.jar" --add-modules org.conetex.runtime.instrument.metrics.cost  -javaagent:agent/target/agent-0.0.1-SNAPSHOT.jar=pathToTransformerJar:../../metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT-fat.jar -m org.conetex.runtime.instrument.test.jar.module/org.conetex.runtime.instrument.test.jar.module.Main


-- patch agent add agent -->  java.lang.NoClassDefFoundError  java.base/java.util.TreeSet.contains(TreeSet.java)
java --module-path "agent/target/agent-0.0.1-SNAPSHOT.jar;interfaces/target/interfaces-0.0.1-SNAPSHOT.jar;counter/target/counter-0.0.1-SNAPSHOT.jar;metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT.jar;test/jar-module/target/jar-module-0.0.1-SNAPSHOT.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm\9.8\asm-9.8.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm-commons\9.8\asm-commons-9.8.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm-tree\9.8\asm-tree-9.8.jar" --patch-module "org.conetex.runtime.instrument.agent=interfaces/target/interfaces-0.0.1-SNAPSHOT.jar;counter/target/counter-0.0.1-SNAPSHOT.jar;metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm\9.8\asm-9.8.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm-commons\9.8\asm-commons-9.8.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm-tree\9.8\asm-tree-9.8.jar" --add-modules org.conetex.runtime.instrument.agent  -javaagent:agent/target/agent-0.0.1-SNAPSHOT.jar=pathToTransformerJar:../../metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT-fat.jar -m org.conetex.runtime.instrument.test.jar.module/org.conetex.runtime.instrument.test.jar.module.Main

-- patch instrument -->  java.lang.IllegalAccessException: java.instrument does not export org.conetex.runtime.instrument.metrics.cost to unnamed module
java --module-path "agent/target/agent-0.0.1-SNAPSHOT.jar;interfaces/target/interfaces-0.0.1-SNAPSHOT.jar;counter/target/counter-0.0.1-SNAPSHOT.jar;metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT.jar;test/jar-module/target/jar-module-0.0.1-SNAPSHOT.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm\9.8\asm-9.8.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm-commons\9.8\asm-commons-9.8.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm-tree\9.8\asm-tree-9.8.jar" --patch-module "java.instrument=interfaces/target/interfaces-0.0.1-SNAPSHOT.jar;counter/target/counter-0.0.1-SNAPSHOT.jar;metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm\9.8\asm-9.8.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm-commons\9.8\asm-commons-9.8.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm-tree\9.8\asm-tree-9.8.jar" -javaagent:agent/target/agent-0.0.1-SNAPSHOT.jar=pathToTransformerJar:../../metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT-fat.jar -m org.conetex.runtime.instrument.test.jar.module/org.conetex.runtime.instrument.test.jar.module.Main



-- patch agent instrument add agent --> IllegalAccessException / superinterface check failed   module java.base does not read unnamed module
java --module-path "agent/target/agent-0.0.1-SNAPSHOT.jar;interfaces/target/interfaces-0.0.1-SNAPSHOT.jar;counter/target/counter-0.0.1-SNAPSHOT.jar;metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT.jar;test/jar-module/target/jar-module-0.0.1-SNAPSHOT.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm\9.8\asm-9.8.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm-commons\9.8\asm-commons-9.8.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm-tree\9.8\asm-tree-9.8.jar" --patch-module "org.conetex.runtime.instrument.agent=interfaces/target/interfaces-0.0.1-SNAPSHOT.jar;counter/target/counter-0.0.1-SNAPSHOT.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm\9.8\asm-9.8.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm-commons\9.8\asm-commons-9.8.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm-tree\9.8\asm-tree-9.8.jar" --patch-module java.base=metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT.jar --add-modules org.conetex.runtime.instrument.agent  -javaagent:agent/target/agent-0.0.1-SNAPSHOT.jar=pathToTransformerJar:../../metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT-fat.jar -m org.conetex.runtime.instrument.test.jar.module/org.conetex.runtime.instrument.test.jar.module.Main

-- patch instrument --> IllegalAccessException / superinterface check failed               module java.instrument does not read unnamed module
java --module-path "agent/target/agent-0.0.1-SNAPSHOT.jar;interfaces/target/classes;counter/target/classes;metrics-cost/target/classes;test/jar-module/target/classes;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm\9.8\asm-9.8.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm-commons\9.8\asm-commons-9.8.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm-tree\9.8\asm-tree-9.8.jar" --patch-module java.instrument=metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT.jar -javaagent:agent/target/agent-0.0.1-SNAPSHOT.jar=pathToTransformerJar:../../metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT-fat.jar -m org.conetex.runtime.instrument.test.jar.module/org.conetex.runtime.instrument.test.jar.module.Main




-- fat patch instrument --> not loaded
java --module-path "agent/target/agent-0.0.1-SNAPSHOT.jar;interfaces/target/classes;counter/target/classes;metrics-cost/target/classes;test/jar-module/target/classes;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm\9.8\asm-9.8.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm-commons\9.8\asm-commons-9.8.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm-tree\9.8\asm-tree-9.8.jar" --patch-module java.instrument=metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT.jar -javaagent:agent/target/agent-0.0.1-SNAPSHOT-fat.jar=pathToTransformerJar:../../metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT-fat.jar -m org.conetex.runtime.instrument.test.jar.module/org.conetex.runtime.instrument.test.jar.module.Main


-- patch agent --> agent unknown not loaded
java --module-path "agent/target/agent-0.0.1-SNAPSHOT.jar;interfaces/target/classes;counter/target/classes;metrics-cost/target/classes;test/jar-module/target/classes;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm\9.8\asm-9.8.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm-commons\9.8\asm-commons-9.8.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm-tree\9.8\asm-tree-9.8.jar" --patch-module org.conetex.runtime.instrument.agent=metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT.jar -javaagent:agent/target/agent-0.0.1-SNAPSHOT.jar=pathToTransformerJar:../../metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT-fat.jar -m org.conetex.runtime.instrument.test.jar.module/org.conetex.runtime.instrument.test.jar.module.Main

-- patch instrument --> not loaded
java --module-path "agent/target/agent-0.0.1-SNAPSHOT.jar;interfaces/target/interfaces-0.0.1-SNAPSHOT.jar;counter/target/counter-0.0.1-SNAPSHOT.jar;metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT.jar;test/jar-module/target/jar-module-0.0.1-SNAPSHOT.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm\9.8\asm-9.8.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm-commons\9.8\asm-commons-9.8.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm-tree\9.8\asm-tree-9.8.jar" --patch-module java.instrument=metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT.jar -javaagent:agent/target/agent-0.0.1-SNAPSHOT.jar=pathToTransformerJar:../../metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT-fat.jar -m org.conetex.runtime.instrument.test.jar.module/org.conetex.runtime.instrument.test.jar.module.Main

-- patch base -->  module java.base does not read module java.instrument       not loaded
java --module-path "agent/target/agent-0.0.1-SNAPSHOT.jar;interfaces/target/interfaces-0.0.1-SNAPSHOT.jar;counter/target/counter-0.0.1-SNAPSHOT.jar;metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT.jar;test/jar-module/target/jar-module-0.0.1-SNAPSHOT.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm\9.8\asm-9.8.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm-commons\9.8\asm-commons-9.8.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm-tree\9.8\asm-tree-9.8.jar" --patch-module "java.base=interfaces/target/interfaces-0.0.1-SNAPSHOT.jar;metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT.jar" -javaagent:agent/target/agent-0.0.1-SNAPSHOT.jar=pathToTransformerJar:../../metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT-fat.jar -m org.conetex.runtime.instrument.test.jar.module/org.conetex.runtime.instrument.test.jar.module.Main

-- patch base -->   not loaded
java --module-path "agent/target/agent-0.0.1-SNAPSHOT.jar;interfaces/target/interfaces-0.0.1-SNAPSHOT.jar;counter/target/counter-0.0.1-SNAPSHOT.jar;metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT.jar;test/jar-module/target/jar-module-0.0.1-SNAPSHOT.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm\9.8\asm-9.8.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm-commons\9.8\asm-commons-9.8.jar;C:\DEV\maven-coba-3.9.9-distributable\local-repository\org\ow2\asm\asm-tree\9.8\asm-tree-9.8.jar" --patch-module "java.base=metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT.jar" -javaagent:agent/target/agent-0.0.1-SNAPSHOT.jar=pathToTransformerJar:../../metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT-fat.jar -m org.conetex.runtime.instrument.test.jar.module/org.conetex.runtime.instrument.test.jar.module.Main







org.conetex.runtime.instrument.agent,
org.conetex.runtime.instrument.metrics.cost,
org.conetex.runtime.instrument.test.jar.module






 */
