package org.conetex.contract.runtime.instrument.metrics.cost;

//import org.conetex.contract.runtime.instrument.RetransformingClassFileTransformer;
import org.conetex.contract.runtime.instrument.counter.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.Set;
import java.util.TreeSet;

public class ClassFileTransformerForCounting implements //Retransforming
        ClassFileTransformer {

    private String mainClassJvmName;

    //@Override
    public void initMainClassJvmName(String mainClassJvmName) {
        this.mainClassJvmName = mainClassJvmName;
    }

    //@Override
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

    //@Override
    public Set<String> getHandledClasses() {
        return handledClasses;
    }

    private final Set<String> transformFailedClasses;

    //@Override
    public Set<String> getTransformFailedClasses() {
        return transformFailedClasses;
    }

    private final Set<String> transformSkippedClasses;

    //@Override
    public Set<String> getTransformSkippedClasses() {
        return transformSkippedClasses;
    }

    public ClassFileTransformerForCounting() {
        this.handledClasses = new TreeSet<>();
        this.transformFailedClasses = new TreeSet<>();
        this.transformSkippedClasses = new TreeSet<>();
    }

    @Override
    public byte[] transform(Module module, ClassLoader loader, String classJvmName, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        System.out.println("t callTransform: " + module + "(module) | " + loader + " (loader) | " + classJvmName +
                " (classJvmName) | " + classBeingRedefined + " (classBeingRedefined) | " +
                (protectionDomain == null ? "null" : protectionDomain.hashCode()) + " (protectionDomain)");
        return transform(loader, classJvmName, classBeingRedefined, protectionDomain, classfileBuffer);
    }

    @Override
    public byte[] transform(ClassLoader loader, String classJvmName, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {

        if (classJvmName.equals(mainClassJvmName)) {
            System.out.println("transform mainClass: " + classJvmName + " ");
        }

        if (this.handledClasses.contains(classJvmName)) {
            System.out.println("t noReTransform: " + loader + " (loader) | " + classJvmName + " (classJvmName) | " +
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

    //@Override
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
        ClassVisitor visitor = new MethodMetricsVisitor(writer);
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

}
