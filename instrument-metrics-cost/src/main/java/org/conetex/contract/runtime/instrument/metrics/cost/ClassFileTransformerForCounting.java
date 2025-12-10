package org.conetex.contract.runtime.instrument.metrics.cost;


import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class ClassFileTransformerForCounting implements ClassFileTransformer {
    private  String mainClassJvmStr;
    private  Instrumentation inst;
    private  Set<String> transformedClasses;
    boolean mainClassLoaded;
    boolean reTransformNotDone;

    Set<String> nottransformedClasses;

    public ClassFileTransformerForCounting(String mainClassJvmStr, Instrumentation inst, Set<String> transformedClasses) {
        this.mainClassJvmStr = mainClassJvmStr;
        this.inst = inst;
        this.transformedClasses = transformedClasses;
        mainClassLoaded = true;
        reTransformNotDone = false;
        nottransformedClasses = new TreeSet<>();
        System.out.println("HALLLOOOO 1");
    }

    @Override
    public byte[] transform(Module module, ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        System.out.println("t callTransform: " + module + "(module" +
                ") | " + loader + " (loader) | " + className + " (className) | " + classBeingRedefined + " (classBeingRedefined) | " + (protectionDomain == null ? "null" : protectionDomain.hashCode()) + " (protectionDomain)");
        return transform(loader, className, classBeingRedefined,
                protectionDomain, classfileBuffer);

    }

    public String toString() {
        return "mfrTransformer";
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {

        if (className.equals("java/util/TreeMap")) {//ist in vererbungshierarchie von main
            System.out.println("BREAK transform: " + className + " (className)");
        }
        if (className.contains("org/objectweb/asm/")
            ||   className.contains("pocASM")
//                                &&  !className.contains("java/lang/Shutdown")
            ||   className.contains("counter/Counter")
        ) {
            System.out.println("BREAK transform: " + className + " (className)");
        }
        //System.out.println("t classLoaded: " + className);
        if (className.equals(mainClassJvmStr)) {
            mainClassLoaded = true;
            System.out.println("t mainClassLoaded: " + mainClassLoaded + " " + className);
//retransform();

        } else {
            if (reTransformNotDone && mainClassLoaded) {
//retransform3(inst, transformedClasses);
                retransform2(inst);
                reTransformNotDone = false;
            }
        }

        //reTransformDone
        if (mainClassLoaded) { //|| className.equals("java/util/TreeMap")

            if (transformedClasses.contains(className)) {
                System.out.println("t noReTransform: " + loader + " (loader) | " + className + " (className) | " + classBeingRedefined + " (classBeingRedefined) | " + (protectionDomain == null ? "null" : protectionDomain.hashCode()) + " (protectionDomain)");
            } else {
                transformedClasses.add(className);
                if (!className.contains("xorg/objectweb/asm/")
                        && !className.contains("xpocASM")
//                                &&  !className.contains("java/lang/Shutdown")
                        && !className.contains("xcounter/Counter")
                ) {
                    //if (!className.startsWith("sun") && !className.startsWith("java") && !className.startsWith("jdk") && !className.startsWith("java/security") && !className.startsWith("java/io") && !className.startsWith("java/lang/") && !className.endsWith("CounterX") && !className.endsWith("AgentX")) {
                    System.out.println("t doTransform: " + loader + " (loader) | " + className + " (className) | " + classBeingRedefined + " (classBeingRedefined) | " + (protectionDomain == null ? "null" : protectionDomain.hashCode()) + " (protectionDomain)");
                    try {
                        //return classfileBuffer;

                        return Transformer.transform2(classfileBuffer);
                    } catch (Throwable e) {
                        System.err.println("t !!! exception 4 " + className + " | " + e.getClass().getName() + " | " + e.getMessage());
                        //e.printStackTrace();
                        //System.out.println("<- trans e ");
                    }
                } else {
                    System.out.println("t noTransform: " + loader + " (loader) | " + className + " (className) | " + classBeingRedefined + " (classBeingRedefined) | " + (protectionDomain == null ? "null" : protectionDomain.hashCode()) + " (protectionDomain)");
                }
            }
        } else {
            System.out.println("t noTransform before main: " + loader + " (loader) | " + className + " (className) | " + classBeingRedefined + " (classBeingRedefined) | " + (protectionDomain == null ? "null" : protectionDomain.hashCode()) + " (protectionDomain)");
            nottransformedClasses.add(className);
        }
        byte[] re = classfileBuffer;
        return re;
        //return Transformer.notransform(classfileBuffer);
    }

    private static void retransform(Instrumentation inst) {
    }
    static void retransform2(Instrumentation inst) {
        //Class[] allclasses = inst.getAllLoadedClasses();
        try {
            inst.retransformClasses(TreeMap.class);
        } catch (UnmodifiableClassException e) {
            System.err.println("t reTRANSClass all classes UnmodifiableClassException " + e.getMessage());
        }
    }


}
