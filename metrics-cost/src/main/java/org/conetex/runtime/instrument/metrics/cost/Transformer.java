package org.conetex.runtime.instrument.metrics.cost;

import org.conetex.runtime.instrument.collection.AvlTree;
import org.conetex.runtime.instrument.counter.Counter;
import org.conetex.runtime.instrument.counter.CountersWeighted;
import org.conetex.runtime.instrument.interfaces.RetransformingClassFileTransformer;
import org.conetex.runtime.instrument.interfaces.arithmetic.ResultLongDividedByInt;
import org.conetex.runtime.instrument.interfaces.counter.LinkedLong;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

public class Transformer implements RetransformingClassFileTransformer {

    /*
prompt:
wenn man mit einem agent per asm java klassen instrumentiert.
welche packages muss man auf jeden fall ausschließen?
welche packages müssen auch von professionellen tools von der instrumentierung ausgeschlossen werden?
mit "professionellen tools" meine ich tools zur instrumentierung zur instrumentierung also zum beispiel byte-buddy oder
IDEs also zum Beispiel intellij-idea, eclipse, android-studio.


---------------------------------------
1. answer chatGPT code guru:

Systemkritische Packages (immer ausschließen)
Es gibt eine Reihe von Systemklassen und Systempaketen, die du immer ausschließen solltest, unabhängig vom Tool zur Instrumentierung. Änderungen an diesen Klassen können die JVM destabilisieren oder schwerwiegende Sicherheitsrisiken verursachen.

Ausschlusskandidaten für die JVM:
Core Java-Klassen (werden vom BootstrapClassLoader geladen):

java.lang.*
Beispiele: java.lang.Object, java.lang.String, java.lang.Thread, java.lang.Class.
java.util.*
Beispiele: java.util.concurrent, java.util.Map, java.util.ArrayList.
java.io.*:
Beispiele: java.io.InputStream, java.io.OutputStream, java.io.File.
java.net.*:
Netzwerkklassen der JVM: java.net.Socket, java.net.URL.
java.nio.*:
Paket für NIO (non-blocking I/O). Beispiele: java.nio.file.Path, java.nio.channels.
Java Reflection API:
java.lang.reflect.* (z. B. Field, Method, Constructor).
ClassLoader- und Bytecode-Klassen:

java.lang.ClassLoader
java.lang.invoke.MethodHandles: Wird verwendet, um dynamische Methodenaufrufe zu ermöglichen.
Security-Klassen:

java.security.* (z. B. java.security.PrivilegedAction).
Module- und interne JVM-Klassen (ab Java 9+):

jdk.internal.*:
Dies enthält interne JVM-APIs, die systemkritisch sind. Beispiele:
jdk.internal.loader.ClassLoaders.
jdk.internal.reflect.*.
java.base-Modul:
Alle Klassen im java.base-Modul sollten tabu sein, da die JVM selbst auf diese Klassen angewiesen ist.


private static final String[] EXCLUDES_PACKAGES = new String[]{
    // Core JVM classes
    "java/lang/Object",              // reason: Fundamental root class of all Java objects; system-critical.
    "java/lang/String",              // reason: Immutable core class, any changes can destabilize the JVM.
    "java/lang/Class",               // reason: Represents classes and interfaces in the JVM; affects class metadata.
    "java/lang/Thread",              // reason: Central to multithreading in Java; critical.
    "java/lang/ThreadLocal",         // reason: Thread-local storage, crucial for concurrency.
    "java/lang/Throwable",           // reason: Base class for exceptions; fundamental for error handling.
    "java/lang/System",              // reason: Entry point for system-related operations (e.g., I/O, environment variables).

    // JVM internal mechanics
    "java/lang/invoke/",             // reason: Dynamic method invocation (MethodHandles, CallSite); cannot safely modify.
    "java/lang/ref/",                // reason: Reference types for GC (SoftReference, WeakReference); GC-sensitive classes.
    "java/lang/reflect/",            // reason: Reflection API classes (e.g., Field, Method); used indirectly by many tools.

    // Collections and functional interfaces
    "java/util/ArrayList",           // reason: Common list implementation; critical performance issues if modified.
    "java/util/HashMap",             // reason: Core map implementation; used widely. Changes could cause issues.
    "java/util/concurrent/",         // reason: Classes for concurrency (ThreadPool, Future); any interference is dangerous.
    "java/util/stream/",             // reason: Streams API classes; unsafe Modifikation kann die Stream-Operationen beeinflussen.
    "java/util/Base64",              // reason: Encoding/decoding functionality, errors in modification affect data handling.

    // I/O classes
    "java/io/File",                  // reason: Represents file systems; changes risk file handling corruption.
    "java/io/InputStream",           // reason: Base class for input streams; critical for I/O operations.
    "java/io/OutputStream",          // reason: Base class for output streams; fundamental for data writing.
    "java/io/BufferedReader",        // reason: I/O buffering (common in file reading); critical performance impact.

    // Networking
    "java/net/Socket",               // reason: Represents a networking socket; manipulation risks breaking communication.
    "java/net/ServerSocket",         // reason: Fundamental class for server-side networking.
    "java/net/URL",                  // reason: Represents Universal Resource Locator; unsafe changes can corrupt URLs.

    // NIO
    "java/nio/file/Path",            // reason: Represents file system paths; critical for modern I/O.
    "java/nio/channels/",            // reason: Handles NIO channels; low-level byte operations.

    // Security
    "java/security/MessageDigest",   // reason: Used for hashing algorithms (MD5, SHA); any change affects core cryptography.
    "java/security/cert/",           // reason: X.509 certificate handling; interference can break TLS/SSL.
    "javax/crypto/",                 // reason: Cryptography APIs (AES, RSA); unsafe modifications affect secure communication.

    // Module system (Java 9+)
    "java/lang/module/",             // reason: Represents the Java module system; interference risks Java 9+ modular applications.

    // Internal JDK classes (always exclude)
    "jdk/internal/loader/",          // reason: Handles class loading internally; breaking these can destabilize the JVM entirely.
    "jdk/internal/math/",            // reason: Math operations (e.g., BigInteger internals); critical for proper operation.
    "jdk/internal/reflect/",         // reason: Reflection internals; unsafe changes affect metadata operations.
    "sun/misc/Unsafe",               // reason: Private APIs for unsafe memory operations; interference can corrupt runtime.

    // ASM-related classes
    "org/objectweb/asm/",            // reason: Critical classes used by ASM itself; infinite recursion risks.
    "org/objectweb/asm/util/",       // reason: Helper utilities for debugging ASM output; instrumentation of them compromises logging.

    // ByteBuddy-related classes
    "net/bytebuddy/",                // reason: ByteBuddy classes (used for dynamic proxies and weavers).

    // IDE-related exclusion
    "com/intellij/",                 // reason: IntelliJ IDEA classes, critical for plugin and IDE operations.
    "org/eclipse/",                  // reason: Eclipse IDE functionality, avoiding interference with IDE runtime processes.

    // Framework-specific exclusion
    "org/springframework/",          // reason: Spring Framework with dynamic proxies and AOP mechanisms (e.g., Beans).
    "org/hibernate/",                // reason: Hibernate ORM proxies; internal optimizations for persistence.

    // Testing frameworks
    "org/junit/",                    // reason: JUnit testing framework; instrumenting tests compromises repeatability.
    "org/testng/",                   // reason: TestNG testing framework functionality.

    // JVM dynamic proxies
    "com/sun/proxy/$Proxy",          // reason: JVM-generated dynamic proxy classes during runtime; should not be modified.
};


---------------------------------------

answer chatGpt pri

Ja – **Profiler sind genau die Gegenbeispiele** zu „nur Applikationscode instrumentieren“.
Sie gehen *bewusst* sehr weit, **aber nie vollständig**. Auch kommerzielle Profiler haben harte Ausschlüsse.

Ich gebe dir das **realistische Bild aus echten Profilern** (Async-Profiler, JProfiler, YourKit, Flight Recorder, Glowroot, Elastic APM).

---

# 1️⃣ Grundprinzip von Profilern

Profiler wollen:

* **Callstacks**
* **Zeit**
* **Allokationen**
* **Locks**
* **IO / CPU / GC**

❗ Aber sie dürfen **nicht**:

* den Classloader brechen
* Rekursionen erzeugen
* den GC beeinflussen
* JIT-Optimierungen zerstören
* Sicherheitsmechanismen umgehen

➡️ Deshalb **instrumentieren Profiler weniger Bytecode als man denkt**.

---

# 2️⃣ Kategorien von Ausschlüssen bei Profilern

## A) **Unantastbare JVM-Klassen**

Diese werden **nie** instrumentiert – selbst von Profilern:

```text
java.lang.Object
java.lang.Class
java.lang.Thread
java.lang.ClassLoader
java.lang.System
java.lang.invoke.*
java.lang.ref.*
sun.misc.Unsafe
jdk.internal.*
```

📌 **Warum?**

* HotSpot geht von *exaktem Bytecode* aus
* JIT setzt Intrinsics voraus
* Sicherheits- & Performance-Garantien

➡️ Beispiel: **Async-Profiler** nutzt *Sampling*, nicht Instrumentierung, genau deswegen.

---

## B) **Classloading & Bytecode-Frameworks**

```text
java.lang.instrument.*
org.objectweb.asm.*
net.bytebuddy.*
javassist.*
```

📌 **Warum?**

* Selbstinstrumentierung
* Rekursion beim Transformieren
* Deadlocks im ClassLoader

➡️ Alle Profiler haben explizite „do not touch“-Listen für diese Packages.

---

## C) **Concurrency & Locks (nur sehr gezielt!)**

Profiler:

* **messen Locks**
* instrumentieren aber **keine Lock-Implementierungen**

```text
java.util.concurrent.locks.*
java.util.concurrent.atomic.*
java.util.concurrent.ForkJoin*
```

📌 **Warum?**

* Instrumentierung verändert Lock-Timing
* Deadlocks & Heisenbugs

➡️ Lock-Profiler nutzen:

* JVMTI Events
* Safepoints
* OS-Timer

---

## D) **GC & Memory Internals**

```text
java.lang.ref.*
java.nio.*
sun.nio.*
jdk.internal.ref.*
```

📌 **Warum?**

* Referenzverarbeitung ist GC-kritisch
* Bytecode-Änderung = Speicherleak oder Crash

➡️ Allocation-Profiling:

* TLAB Hooks
* JVMTI ObjectAllocation events
* Stack walking

---

## E) **JIT & Intrinsics**

```text
java.lang.Math
java.lang.String
java.lang.System.arraycopy
```

📌 **Warum?**

* HotSpot ersetzt diese Methoden durch native Intrinsics
* Instrumentierung verhindert Optimierungen

➡️ Profiler *lesen* hier nur CPU-Zeit, kein Bytecode-Edit.

---

## F) **Eigene Profiler-Klassen**

Alle Profiler haben **strikte Self-Exclusion**:

```text
com.yourkit.*
com.jprofiler.*
org.asyncprofiler.*
```

📌 **Warum?**

* Endlosschleifen
* verfälschte Messwerte

---

## G) **Logging & Monitoring Libraries**

```text
org.slf4j.*
ch.qos.logback.*
org.apache.logging.*
```

📌 **Warum?**

* Logging passiert überall
* Rekursion + massiver Overhead

➡️ Profiler erfassen Logging **nur als Zeitanteil**, nicht instrumentiert.

---

## H) **Native / JNI Boundaries**

```text
java.lang.Runtime
java.lang.Process*
sun.nio.ch.*
```

📌 **Warum?**

* Native Calls lassen sich nicht sauber instrumentieren
* Übergänge werden über Stack-Sampling erfasst

---

# 3️⃣ Konkrete Beispiele realer Profiler

## 🔥 Async-Profiler

* **keine Bytecode-Instrumentierung** für Timing
* Sampling via:

  * perf events
  * safepoint bias
* Instrumentiert **nur optional**:

  * Allocation tracking
* Ausschluss: fast alles JVM-Core

---

## 🧠 JProfiler / YourKit

* Kombination aus:

  * JVMTI
  * minimaler Bytecode-Instrumentierung
* Ausschlusslisten enthalten:

  * `java.lang.*`
  * `java.util.concurrent.*`
  * `sun.*`
  * `jdk.internal.*`

---

## 📊 Java Flight Recorder (JFR)

* **gar keine Bytecode-Instrumentierung**
* Events direkt aus HotSpot

➡️ Maximale Sicherheit, minimale Verzerrung

---

## 🧪 Glowroot / Elastic APM

* instrumentieren:

  * Servlets
  * JDBC
  * HTTP Clients
* schließen aus:

  * JDK
  * Framework-Internals
  * Logging

---

# 4️⃣ Beispiel: Typische Profiler-Exclusion-Liste (realistisch)

```java
String[] PROFILER_EXCLUDES = {

    // JVM Core
    "java/lang/",
    "jdk/",
    "sun/",

    // Classloading & Instrumentation
    "java/lang/instrument/",
    "org/objectweb/asm/",
    "net/bytebuddy/",
    "javassist/",

    // Concurrency & GC
    "java/util/concurrent/",
    "java/lang/ref/",
    "sun/misc/",
    "jdk/internal/",

    // JIT Intrinsics
    "java/lang/String",
    "java/lang/Math",
    "java/lang/System",

    // Logging
    "org/slf4j/",
    "ch/qos/logback/",
    "org/apache/logging/",

    // Profiler itself
    "com/yourprofiler/",
    "org/asyncprofiler/"
};
```

---

# 5️⃣ Wichtigste Erkenntnis (die viele übersehen)

> **Profiler instrumentieren nicht „viel“,
> sie instrumentieren „strategisch“**.

Je näher du an:

* ClassLoader
* GC
* JIT
* Locks
* Threading

kommst, desto weniger Bytecode-Instrumentierung –
und desto mehr **JVMTI / Sampling / VM Events**.

---

Wenn du willst, können wir als Nächstes:

* 🧬 **welche Klassen Profiler *gezielt* instrumentieren**
* ⚙️ **Hybrid-Architektur: ASM + JVMTI**
* 📉 **Messverzerrungen durch Instrumentierung**
* 🧪 **Benchmark: Instrumentierung vs Sampling**

durchgehen.


---------------------------------------






    */
    public static final String[] UNTRANSFORMABLE = {
            // type names carefully: class-names do not end with "/" so switch from "startsWith" to "equals"

            "java/lang/invoke/" , // needed for bootstrap calls

            // unblock test-classes
            "org/conetex/runtime/instrument/test/jar/MainJar"            ,
            "org/conetex/runtime/instrument/test/jar/MainJarTest"        ,
            //"org/conetex/runtime/instrument/test/count/opcodes/MainCountOpcodes",
            "org/conetex/runtime/instrument/test/count/opcodes/MainCountOpcodesTest",
            //"org/conetex/runtime/instrument/test/jar/module/Main"     ,
            //"org/conetex/runtime/instrument/test/jar/module/MainTest"

            // "java/lang/invoke/MethodHandle$1" ,
            // "sun" ,
            // "com/intellij/rt" ,
            // "java/io/UnsupportedEncodingException",

            /* todo only needed for dynamic mode and it isn't enough yet...
            "java/lang/reflect" ,
            "com/intellij/rt" ,
            "jdk.internal.loader.ClassLoaders" ,
            "sun/invoke" ,
            "java/security" ,
            "java/lang/Throwable",
            "java/lang"
            */
    };

    public static final String[] BLOCKED_PACKAGES = {
            "org/conetex/runtime/instrument",
            "org/objectweb/asm/"
    };

    public static final int STATUS_BLOCKED = 403;

    private String mainClassJvmName;

    @Override
    public void initMainClassJvmName(String mainClassJvmName) {
        this.mainClassJvmName = mainClassJvmName;
    }

    private int visitMethod = Visitor.VISIT_MV_STATIC;
    
    @Override
    public void initTransformer(String argsCommaSeparated){
        System.out.println("||-> " + argsCommaSeparated);
        if(argsCommaSeparated.contains("visit_mv_static_count_opcodes")){
            System.out.println("||--> " + argsCommaSeparated);
            this.visitMethod = Visitor.VISIT_MV_STATIC_COUNT_OPCODES;
            String[] args = argsCommaSeparated.split(",");
            String prefix = "instrumentationTargetClass";
            for (String arg : args) {
                System.out.println("||---> " + arg);
                if (arg.startsWith(prefix)) {
                    System.out.println("||----> " + arg.substring(prefix.length()));
                    instrumentationTargetClasses.add(arg.substring(prefix.length()));
                }
            }
        }
        else if(argsCommaSeparated.contains("visit_mv_static")){
            this.visitMethod = Visitor.VISIT_MV_STATIC;
        }
        else if(argsCommaSeparated.contains("visit_mv_dynamic")){
            this.visitMethod = Visitor.VISIT_MV_DYNAMIC;
        }
        else {
            this.visitMethod = Visitor.VISIT_MV_DEFAULT;
        }
    }

    public void addToHandledClasses(String classJvmName) {
        this.handledClasses.add(classJvmName);
    }

    @Override
    public void resetCounters() {
        Counters.reset();
    }

    @Override
    public void blockIncrement(boolean incrementationBlocked) {
        Counters.blockIncrement(incrementationBlocked);
    }

    private final Set<String> instrumentationTargetClasses;

    private final Set<String> handledClasses;

    @Override
    public Set<String> getHandledClasses() {
        return handledClasses;
    }

    private final Set<String> transformFailedClasses;

    @Override
    public Set<String> getTransformFailedClasses() {
        return transformFailedClasses;
    }

    private final Set<String> transformSkippedClasses;

    @Override
    public Set<String> getTransformSkippedClasses() {
        return transformSkippedClasses;
    }

    public Transformer() {
        this.handledClasses = new TreeSet<>();
        this.transformFailedClasses = new TreeSet<>();
        this.transformSkippedClasses = new TreeSet<>();
        this.instrumentationTargetClasses = new TreeSet<>();

        // todo is this solved in general?
        // calling this leads to
        // load all classes, before they are needed by transform.
        // this is necessary to avoid transformation loops
        //this.getConfig();
        //System.out.println("Counters: " + Counters.class.getModule() + "(module) " + Counters.class.getClassLoader() + "(classLoader)");
        //Counters.reset();
    }

    @Override
    public byte[] transform(Module module, ClassLoader loader, String classJvmName, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classFileBuffer) {
        System.out.println("transform: " + loader + " (loader) | " + classJvmName +
                " (classJvmName) | " + classBeingRedefined + " (classBeingRedefined) | " +
                (protectionDomain == null ? "null" : protectionDomain.hashCode()) + " (protectionDomain) | " + module + "(module)");
        return transform(loader, classJvmName, classBeingRedefined, protectionDomain, classFileBuffer);
    }

    private static boolean transformInProgress = false;

    @Override
    public byte[] transform(ClassLoader loader, String classJvmName, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classFileBuffer) {
        if(transformInProgress){
            throw new CyclicCallException("circle");
        }
        transformInProgress = true;
        try {
            return transformIntern( loader,  classJvmName,  classBeingRedefined,
                     protectionDomain,  classFileBuffer);
        }
        finally{
            transformInProgress = false;
        }
    }

    private byte[] transformIntern(ClassLoader loader, String classJvmName, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classFileBuffer) {

        if (classJvmName.equals(mainClassJvmName)) {
            System.out.println("transform mainClass: " + classJvmName + " ");
        }

        if (this.handledClasses.contains(classJvmName)) {
            System.out.println("transform already done: " + loader + " (loader) | " + classJvmName + " (classJvmName) | " +
                    classBeingRedefined + " (classBeingRedefined) | " +
                    (protectionDomain == null ? "null" : protectionDomain.hashCode()) + " (protectionDomain)");
            return classFileBuffer;
        }

        this.handledClasses.add(classJvmName);

        if(!this.instrumentationTargetClasses.isEmpty()){
            if(!this.instrumentationTargetClasses.contains(classJvmName)){
                System.out.println("transform not whitelisted: " + loader + " (loader) | " + classJvmName + " (classJvmName) | " +
                        classBeingRedefined + " (classBeingRedefined) | " +
                        (protectionDomain == null ? "null" : protectionDomain.hashCode()) + " (protectionDomain)");
                return classFileBuffer;
            }
        }

        for(String untransformable : UNTRANSFORMABLE){
            if (
                    classJvmName.equals(untransformable) ||                                     // class
                    (untransformable.endsWith("/") && classJvmName.startsWith(untransformable)) // package
            ) {
                System.out.println("t noTransform: " + loader + " (loader) | " + classJvmName + " (classJvmName) | " +
                        classBeingRedefined + " (classBeingRedefined) | " +
                        (protectionDomain == null ? "null" : protectionDomain.hashCode()) + " (protectionDomain)");
                // skip transform
                this.transformSkippedClasses.add(classJvmName);
                return classFileBuffer;
            }
        }

        for(String blockedPackage : BLOCKED_PACKAGES){
            if ( classJvmName.startsWith(blockedPackage) ) {
                System.err.println("blocked transform: " + loader + " (loader) | " + classJvmName + " (classJvmName) | " +
                        classBeingRedefined + " (classBeingRedefined) | " +
                        (protectionDomain == null ? "null" : protectionDomain.hashCode()) + " (protectionDomain)");
                block(classJvmName);
            }
        }

        System.out.println("t doTransform: " + loader + " (loader) | " + classJvmName + " (classJvmName) | " +
                classBeingRedefined + " (classBeingRedefined) | " +
                (protectionDomain == null ? "null" : protectionDomain.hashCode()) + " (protectionDomain)");

        try {
            return transform(classFileBuffer, this.visitMethod);
        }
        catch(CyclicCallException e){
            System.err.println("circle " + classJvmName + " | " + e.getClass().getName() + " | " +
                    e.getMessage());
            this.transformFailedClasses.add(classJvmName);
        }
        catch (Throwable e) {
            System.err.println("t !!! exception 4 " + classJvmName + " | " + e.getClass().getName() + " | " +
                    e.getMessage());
            this.transformFailedClasses.add(classJvmName);
        }
        return classFileBuffer;
    }

    private void block(String classJvmName) {
        System.err.println("blocked " + classJvmName);
        System.out.println("blocked " + classJvmName);
        // BLOCK class
        // since this class should have been loaded before transformer was added to instrumentation.
        // retransform for this class should have been skipped.
        // todo is there any chance to make this nicer?
        //throw new RuntimeException("blocked " + classJvmName);
        //Runtime.getRuntime().halt(STATUS_BLOCKED);
        System.exit(STATUS_BLOCKED);
    }

    @Override
    public void triggerRetransform(Instrumentation inst, Class<?>[] allClasses) {
        classLoop: for (Class<?> clazz : allClasses) {
            String classJvmName = clazz.getName().replace('.', '/');

//            System.out.println("retransform .....: '" + classJvmName + "' (classJvmName) '" + clazz.getModule() + "' (module) '" + clazz.getClassLoader() + "' (classLoader)");

            if(   this.handledClasses.contains( classJvmName )   ) {
                System.out.println("retransform obsolete: '" + classJvmName +
                        "' (classJvmName) is already transformed");
                continue;
            }

            if (! inst.isModifiableClass(clazz)) {
                System.out.println("retransform skipped for unmodifiable: '" + classJvmName + "' (classJvmName)");
                continue;
            }
			
			// maybe obsolete
            for(String untransformable : UNTRANSFORMABLE){
                if (
                        classJvmName.equals(untransformable) ||                                     // class
                        (untransformable.endsWith("/") && classJvmName.startsWith(untransformable)) // package
                ) {  // skip retransform
                    System.out.println("retransform skipped: '" + classJvmName + "' (classJvmName)");
                    continue classLoop;
                }
            }
            for(String blockedPackage : BLOCKED_PACKAGES){
                if ( classJvmName.startsWith(blockedPackage) ) {
                    block(classJvmName);
                }
            }

            try {
                inst.retransformClasses(clazz);
            } catch (UnmodifiableClassException e) {
                System.err.println("retransform failed for'" + clazz + "' (class). UnmodifiableClassException: " +
                        e.getMessage());
                continue;
            }
            System.out.println("retransform triggered for '" + clazz + "' (class) || '" + classJvmName +
                    "' (classJvmName) -->");
        }
    }

    public static class CyclicCallException extends IllegalStateException {
        public CyclicCallException(String message) {
            super(message);
        }
    }

    
    private static synchronized byte[] transform(byte[] classBytes, int visitMethod) {
        System.out.println(" classWriter->");
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
        //ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        ClassVisitor visitor = new Visitor(writer, visitMethod);
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        //reader.accept(visitor, ClassReader.SKIP_FRAMES);
        byte[] re = writer.toByteArray();
        System.out.println(" <-classWriter");
        return re;
    }

    @SuppressWarnings("unused")
    public static byte[] noRealTransform(byte[] classBytes) {
        // todo why does this lead to errors?
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
        return writer.toByteArray();
    }

    @SuppressWarnings("unused")
    public static byte[] noTransform(byte[] classBytes) {
        // todo why does this lead to errors?
        return classBytes;
    }


    public ResultLongDividedByInt[] report2(){
        if(this.visitMethod == Visitor.VISIT_MV_STATIC_COUNT_OPCODES){
            System.out.println("counter;opCode;value");
            for(AvlTree.Entry<String, Counter> counterOfOpcode : CountOpcodes.COUNTERS){
                LinkedLong counterNode = counterOfOpcode.value().peek();
                System.out.print(counterOfOpcode.key() + ";" + counterNode.getValue());
                counterNode = counterNode.getPrevious();
                while(counterNode != null && counterNode.hasPrevious()){
                    counterNode = counterNode.getPrevious();
                    System.out.print("." + counterNode.getValue());
                }
                System.out.println();
            }
            System.out.println("---------------");
            Visitor.printOpCodes(System.out);

            return null;
        }
        else{
            ResultLongDividedByInt[] totalCost =  Counters.CONFIG.average();;
            System.out.println("overall costs:");
            System.out.println(Arrays.toString(totalCost));
            return totalCost;
        }
    }

    private static final Path REPORT_FILE = Paths.get("target", "instrumentation-report.txt");

    @Override
    public ResultLongDividedByInt[] report() {
        try (PrintStream out = new PrintStream(
                Files.newOutputStream(
                        REPORT_FILE,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND
                ),
                true, // autoFlush
                StandardCharsets.UTF_8
        )) {

            if (this.visitMethod == Visitor.VISIT_MV_STATIC_COUNT_OPCODES) {

                out.println("counter;opCode;value");

                for (AvlTree.Entry<String, Counter> counterOfOpcode : CountOpcodes.COUNTERS) {
                    LinkedLong counterNode = counterOfOpcode.value().peek();

                    out.print(counterOfOpcode.key() + ";" + counterNode.getValue());

                    counterNode = counterNode.getPrevious();
                    while (counterNode != null && counterNode.hasPrevious()) {
                        counterNode = counterNode.getPrevious();
                        out.print("." + counterNode.getValue());
                    }
                    out.println();
                }

                out.println("---------------");
                Visitor.printOpCodes(out); // Visitor bekommt eine PrintStream-Variante

                return null;

            } else {
                ResultLongDividedByInt[] totalCost = Counters.CONFIG.average();
                out.println("overall costs:");
                out.println(Arrays.toString(totalCost));
                return totalCost;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }




}
