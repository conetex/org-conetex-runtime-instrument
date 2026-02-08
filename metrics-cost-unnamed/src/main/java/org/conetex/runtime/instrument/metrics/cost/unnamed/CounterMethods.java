package org.conetex.runtime.instrument.metrics.cost.unnamed;

import java.lang.invoke.*;
import java.lang.reflect.Field;

public class CounterMethods {

    // Static Counters in Bootstrap (initialized via reflection from Counters)
    public static MethodHandle ARITHMETIC_ADD_SUB_NEG;
    public static MethodHandle ARITHMETIC_DIV_REM;
    public static MethodHandle ARITHMETIC_MUL;
    public static MethodHandle ARRAY_LOAD;
    public static MethodHandle ARRAY_NEW;
    public static MethodHandle ARRAY_STORE;
    public static MethodHandle COMPARE_INT;
    public static MethodHandle COMPARE_LONG;
    public static MethodHandle COMPARE_OBJECT;
    public static MethodHandle EXCEPTION_THROW;
    public static MethodHandle FIELD_LOAD;
    public static MethodHandle FIELD_STORE;
    public static MethodHandle JUMP;
    public static MethodHandle METHOD_CALL;
    public static MethodHandle METHOD_ENTRY;
    public static MethodHandle MONITOR;
    public static MethodHandle VARIABLE_LOAD;
    public static MethodHandle VARIABLE_STORE;
    public static MethodHandle TYPE_CHECK;

    static {
        // Log message to confirm loading of Bootstrap
        System.out.println("Bootstrap loaded: " +
                CounterMethods.class + " (class) - " +
                CounterMethods.class.getModule() + " (module) - " +
                CounterMethods.class.getClassLoader() + " (loader)");

        try {
            // Dynamically load the Counters class using Class.forName
            System.out.println("use SystemClassLoader '" + ClassLoader.getSystemClassLoader() + "' to load Counters.");
            Class<?> countersClass = Class.forName("org.conetex.runtime.instrument.metrics.cost.Counters", true, ClassLoader.getSystemClassLoader());
            Class<?> incrementableClass = Class.forName("org.conetex.runtime.instrument.interfaces.counter.Incrementable", true, ClassLoader.getSystemClassLoader());

            // Dynamically initialize all Counters fields using reflection
            initializeCounters(countersClass, incrementableClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot bootstrap: Counters.class not found. Error: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error while initializing Bootstrap counters: " + e.getMessage(), e);
        }

        // warmup - needed to have "java/lang/invoke/" loaded
        try {
            CallSite callToNirvana = Bootstrap.callSite(MethodHandles.lookup(), "nirvana", MethodType.methodType(void.class), CounterMethods.class);
            callToNirvana.getTarget().invoke();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    // Method to initialize counters dynamically
    private static void initializeCounters(Class<?> countersClass, Class<?> incrementableClass) throws Exception {

        // Loop through all declared fields in the Bootstrap class
        for (Field bootstrapField : CounterMethods.class.getDeclaredFields()) {
            // Make the field accessible (for private or protected access modifiers)
            //bootstrapField.setAccessible(true);

            // Check if the field is static and of type Incrementable
            if (
                    java.lang.reflect.Modifier.isStatic(bootstrapField.getModifiers())
                 && MethodHandle.class.isAssignableFrom(bootstrapField.getType())
            ) {
                String bootstrapFieldName = bootstrapField.getName();

                // Set the value of the Bootstrap field to point to the instance from Counters
                bootstrapField.set(null, createHandle(bootstrapFieldName, countersClass, incrementableClass) );
                System.out.println("Initialized Bootstrap field '" + bootstrapFieldName + "' with value from Counters.");
            }
        }

    }

    private static MethodHandle createHandle(String bootstrapFieldName, Class<?> countersClass, Class<?> incrementableClass) throws Exception {

        // Initialize the corresponding field in Bootstrap using the Counters field's value
        Field field;
        try {
            // Lookup Counters field that matches the Bootstrap field name
            field = countersClass.getDeclaredField(bootstrapFieldName);
            // Make the field accessible (for private or protected access modifiers)
            //field.setAccessible(true);

            // Use MethodHandles to find the "increment" method in Incrementable
            MethodHandle incrementHandle = MethodHandles.lookup()
                    .findVirtual(incrementableClass, "increment", MethodType.methodType(void.class));
            Object counterInstance = field.get(null);
            MethodHandle boundHandle = incrementHandle.bindTo(counterInstance);
            CallSite callSite = new ConstantCallSite(boundHandle);
            return callSite.getTarget();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("No matching field found in Counters for '" + bootstrapFieldName + "': " + e.getMessage(), e);
        }

    }

    // static CallSites
    @SuppressWarnings("unused")
    public static void incrementArithmeticAddSubNeg() throws Throwable {
        CounterMethods.ARITHMETIC_ADD_SUB_NEG.invoke();
    }

    @SuppressWarnings("unused")
    public static void incrementArithmeticDivRem() throws Throwable {
        CounterMethods.ARITHMETIC_DIV_REM.invoke();
    }

    @SuppressWarnings("unused")
    public static void incrementArithmeticMul() throws Throwable {
        CounterMethods.ARITHMETIC_MUL.invoke();
    }

    @SuppressWarnings("unused")
    public static void incrementArrayLoad() throws Throwable {
        CounterMethods.ARRAY_LOAD.invoke();
    }

    @SuppressWarnings("unused")
    public static void incrementArrayNew() throws Throwable {
        CounterMethods.ARRAY_NEW.invoke();
    }

    @SuppressWarnings("unused")
    public static void incrementArrayStore() throws Throwable {
        CounterMethods.ARRAY_STORE.invoke();
    }

    @SuppressWarnings("unused")
    public static void incrementCompareInt() throws Throwable {
        CounterMethods.COMPARE_INT.invoke();
    }

    @SuppressWarnings("unused")
    public static void incrementCompareLong() throws Throwable {
        CounterMethods.COMPARE_LONG.invoke();
    }

    @SuppressWarnings("unused")
    public static void incrementCompareObject() throws Throwable {
        CounterMethods.COMPARE_OBJECT.invoke();
    }

    @SuppressWarnings("unused")
    public static void incrementExceptionThrow() throws Throwable {
        CounterMethods.EXCEPTION_THROW.invoke();
    }

    @SuppressWarnings("unused")
    public static void incrementFieldLoad() throws Throwable {
        CounterMethods.FIELD_LOAD.invoke();
    }

    @SuppressWarnings("unused")
    public static void incrementFieldStore() throws Throwable {
        CounterMethods.FIELD_STORE.invoke();
    }

    @SuppressWarnings("unused")
    public static void incrementJump() throws Throwable {
        CounterMethods.JUMP.invoke();
    }

    @SuppressWarnings("unused")
    public static void incrementMethodCall() throws Throwable {
        CounterMethods.METHOD_CALL.invoke();
    }

    @SuppressWarnings("unused")
    public static void incrementMethodEntry() throws Throwable {
        CounterMethods.METHOD_ENTRY.invoke();
    }

    @SuppressWarnings("unused")
    public static void incrementMonitor() throws Throwable {
        CounterMethods.MONITOR.invoke();
    }

    @SuppressWarnings("unused")
    public static void incrementVariableLoad() throws Throwable {
        CounterMethods.VARIABLE_LOAD.invoke();
    }

    @SuppressWarnings("unused")
    public static void incrementVariableStore() throws Throwable {
        CounterMethods.VARIABLE_STORE.invoke();
    }

    @SuppressWarnings("unused")
    public static void incrementTypeCheck() throws Throwable {
        CounterMethods.TYPE_CHECK.invoke();
    }





    // todo delete
    /* static {
        System.out.println("Bootstrap loaded: " + Bootstrap.class + " (class) - " + Bootstrap.class.getModule() + " (module) - " + Bootstrap.class.getClassLoader() + " (loader)");
        try {
            Class.forName("org.conetex.runtime.instrument.metrics.cost.Counters", true, null);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("can not bootstrap " + e.getMessage());
        }


    }*/
    // todo delete
    private static int i = 0;
    private static boolean incrementationBlocked = false;
    public static synchronized void nirvana(){
        //System.out.println("daya");
        if (Bootstrap.bootstrapInProgress) {
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

