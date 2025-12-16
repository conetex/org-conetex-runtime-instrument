package org.conetex.contract.runtime.instrument.metrics.cost;

import org.conetex.contract.runtime.instrument.Report;
import org.conetex.contract.runtime.instrument.interfaces.Counter;
import org.conetex.contract.runtime.instrument.interfaces.RetransformingClassFileTransformer;
import org.conetex.contract.runtime.instrument.counter.*;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

public class Transformer implements RetransformingClassFileTransformer {

    private String mainClassJvmName;

    @Override
    public void initMainClassJvmName(String mainClassJvmName) {
        this.mainClassJvmName = mainClassJvmName;
    }

    @Override
    public Counter[] getCounters() {
        return new Counter[]{
                ArithmeticAddSubNeg.getHead(),
                ArithmeticDivRem.getHead(),
                ArithmeticMul.getHead(),

                ArrayLoad.getHead(),
                ArrayNew.getHead(),
                ArrayStore.getHead(),

                CompareInt.getHead(),
                CompareLong.getHead(),
                CompareObject.getHead(),

                ExceptionThrow.getHead(),

                FieldLoad.getHead(),
                FieldStore.getHead(),

                Jump.getHead(),

                MethodCall.getHead(),
                MethodEntry.getHead(),

                Monitor.getHead(),

                VariableLoad.getHead(),
                VariableStore.getHead(),

                TypeCheck.getHead()
        };
    }

    private final int[] weights;

    @Override
    public int[] getCounterWeights() {
        return this.weights;
    }

    @Override
    public void resetCounters() {
        ArithmeticAddSubNeg.reset();
        ArithmeticDivRem.reset();
        ArithmeticMul.reset();
        ArrayLoad.reset();
        ArrayNew.reset();
        ArrayStore.reset();
        CompareInt.reset();
        CompareLong.reset();
        CompareObject.reset();
        ExceptionThrow.reset();
        FieldLoad.reset();
        FieldStore.reset();
        Jump.reset();
        MethodCall.reset();
        MethodEntry.reset();
        Monitor.reset();
        VariableLoad.reset();
        VariableStore.reset();
        TypeCheck.reset();
    }

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

        // creating this array leads to
        // load all classes, before they are needed by transform.
        // this is necessary to avoid transformation loops
        this.resetCounters();
        this.getCounters();

        this.weights = new int[] {
                1, // ArithmeticAddSubNeg
                1, // ArithmeticDivRem
                1, // ArithmeticMul

                1, // ArrayLoad
                1, // ArrayNew
                1, // ArrayStore

                1, // CompareInt
                1, // CompareLong
                1, // CompareObject

                1, // ExceptionThrow

                1, // FieldLoad
                1, // FieldStore

                1, // Jump

                1, // MethodCall
                1, // MethodEntry

                1, // Monitor

                1, // VariableLoad
                1, // VariableStore

                1  // TypeCheck
        };
    }

    @Override
    public byte[] transform(Module module, ClassLoader loader, String classJvmName, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        System.out.println("transform: " + loader + " (loader) | " + classJvmName +
                " (classJvmName) | " + classBeingRedefined + " (classBeingRedefined) | " +
                (protectionDomain == null ? "null" : protectionDomain.hashCode()) + " (protectionDomain) | " + module + "(module)");
        return transform(loader, classJvmName, classBeingRedefined, protectionDomain, classfileBuffer);
    }

    @Override
    public byte[] transform(ClassLoader loader, String classJvmName, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {

        if (classJvmName.equals(mainClassJvmName)) {
            System.out.println("transform mainClass: " + classJvmName + " ");
        }

        if (this.handledClasses.contains(classJvmName)) {
            System.out.println("transform: " + loader + " (loader) | " + classJvmName + " (classJvmName) | " +
                    classBeingRedefined + " (classBeingRedefined) | " +
                    (protectionDomain == null ? "null" : protectionDomain.hashCode()) + " (protectionDomain)");
            return classfileBuffer;
        }

        this.handledClasses.add(classJvmName);

        if (classJvmName.contains("org/objectweb/asm/")
                || classJvmName.startsWith("org/conetex/contract/runtime/instrument")
                || classJvmName.startsWith("org/conetex/contract/runtime/Agent")
        ) { // skip transform
            System.out.println("t noTransform: " + loader + " (loader) | " + classJvmName + " (classJvmName) | " +
                    classBeingRedefined + " (classBeingRedefined) | " +
                    (protectionDomain == null ? "null" : protectionDomain.hashCode()) + " (protectionDomain)");
            this.transformSkippedClasses.add(classJvmName);
            return classfileBuffer;
        }

        System.out.println("t doTransform: " + loader + " (loader) | " + classJvmName + " (classJvmName) | " +
                classBeingRedefined + " (classBeingRedefined) | " +
                (protectionDomain == null ? "null" : protectionDomain.hashCode()) + " (protectionDomain)");

        try {
            return transform(classfileBuffer);
        }
        catch (Throwable e) {
            System.err.println("t !!! exception 4 " + classJvmName + " | " + e.getClass().getName() + " | " +
                    e.getMessage());
            this.transformFailedClasses.add(classJvmName);
        }
        return classfileBuffer;
    }

    @Override
    public void triggerRetransform(Instrumentation inst, Class<?>[] allClasses) {
        for (Class<?> clazz : allClasses) {
            String classJvmName = clazz.getName().replace('.', '/');

            System.out.println("retransform .....: '" + classJvmName + "' (classJvmName)");


            if(   this.handledClasses.contains( classJvmName )   ) {
                System.out.println("retransform obsolete: '" + classJvmName +
                        "' (classJvmName) is already transformed");
                continue;
            }

            if (! inst.isModifiableClass(clazz)) {
                System.out.println("retransform skipped for unmodifiable: '" + classJvmName + "' (classJvmName)");
                continue;
            }

/*
            // TODO maybe obsolete
            if( classJvmName.contains("org/objectweb/asm/") ||
                    classJvmName.startsWith("org/conetex/contract/runtime/instrument")
                    || classJvmName.startsWith("org/conetex/contract/runtime/Agent")
//                    || classJvmName.startsWith("java/util/TreeSet")
            ) { // skip retransform
                System.out.println("retransform skipped: '" + classJvmName + "' (classJvmName)");
                continue;
            }
*/
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

    private static byte[] transform(byte[] classBytes) {
        System.out.println(" classWriter->");
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
        ClassVisitor visitor = new Visitor(writer);
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        byte[] re = writer.toByteArray();
        System.out.println(" <-classWriter");
        return re;
    }

    public static byte[] noRealTransform(byte[] classBytes) {
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
        return writer.toByteArray();
    }

    public static byte[] noTransform(byte[] classBytes) {
        return classBytes;
    }

    @Override
    public long[] report(){
        System.out.println("Long min:  " + Long.MIN_VALUE);
        System.out.println("Long max:  " + Long.MAX_VALUE);
        System.out.println("AbstractCounter min:  " + AbstractCounter.MIN_VALUE);
        System.out.println("AbstractCounter max:  " + AbstractCounter.MAX_VALUE);
        System.out.println("Shutdown-Hook started.");
        long[] totalCost =  Report.calculateTotalCost(this);
        System.out.println("overall costs:");
        System.out.println(Arrays.toString(totalCost));
        return totalCost;
    }
}
