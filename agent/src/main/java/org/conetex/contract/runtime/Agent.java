package org.conetex.contract.runtime;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Agent {

    //public static final String CLASS_FILE_TRANSFORMER_CLASS = "counter.MyClassFileTransformer";
    public static final String PATH_TO_TRANSFORMER_JAR = "pathToTransformerJar";

    public static void agentmain(
            String agentArgs, Instrumentation inst) {
        premain(agentArgs, inst);
    }

    // USAGE: -javaagent:C:\_PROJ\GITHUB\org.conetex.contract.runtime\agent\target\agent-1.0-SNAPSHOT.jar=C:\_PROG\eclipse-java-2025-06WSpaces\workspaceA\counter\target\counter-0.0.2-SNAPSHOT-jar-with-dependencies.jar
    // USAGE: -javaagent:/agent/target/agent-1.0-SNAPSHOT.jar=C:\_PROG\eclipse-java-2025-06WSpaces\workspaceA\counter\target\counter-0.0.2-SNAPSHOT-jar-with-dependencies.jar
    public static void premain(String agentArgs, Instrumentation inst) {
        Path agentPath = null;
        try {
            agentPath = Paths.get(Agent.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        Path agentDir = Files.isDirectory(agentPath) ? agentPath : agentPath.getParent();

        Map<String, String> args = parseAgentArgs(agentArgs);

        String bootstrapJarPath = args.get(PATH_TO_TRANSFORMER_JAR);
        if (bootstrapJarPath == null || bootstrapJarPath.isEmpty()) {
            throw new IllegalArgumentException("Missing required agent argument: " + PATH_TO_TRANSFORMER_JAR + "=<path-to-bootstrap-jar>");
        }
        Path bootstrapPath = agentDir.resolve(bootstrapJarPath);
        JarFile bootstrapJar = null;
        try {
            bootstrapJar = new JarFile(bootstrapPath.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String appendToBootstrapClassLoaderSearchStr = getMainAttributeFromJar(bootstrapJar, "appendToBootstrapClassLoaderSearch");
        assert appendToBootstrapClassLoaderSearchStr != null;
        if(! appendToBootstrapClassLoaderSearchStr.equals("false")){
            inst.appendToBootstrapClassLoaderSearch(bootstrapJar);
        }

        System.out.println("Redefine supported: " + inst.isRedefineClassesSupported());
        System.out.println("Retransform supported: " + inst.isRetransformClassesSupported());
        System.out.println("NativeMethodPrefix supported: " + inst.isNativeMethodPrefixSupported());

        Class<?>[] classes = inst.getAllLoadedClasses();

        String command = System.getProperty("sun.java.command");
        System.out.println("sun.java.command: " + command);
        assert command != null;

        String mainClassJavaStr = null;
        if(command.endsWith(".jar")) {
            mainClassJavaStr = getMainAttributeFromJar(command, "Main-Class");
        }
        else {
            mainClassJavaStr = command;
        }
        System.out.println("mainClassStr from sun.java.command: " + mainClassJavaStr);
        assert mainClassJavaStr != null;
        String mainClassJvmStr = mainClassJavaStr.replace('.', '/');
        System.out.println("mainClassJvmStr from sun.java.command: " + mainClassJvmStr);

        classes = inst.getAllLoadedClasses();
        System.out.println("Redefine supported: " + inst.isRedefineClassesSupported());
        System.out.println("Retransform supported: " + inst.isRetransformClassesSupported());
        System.out.println("NativeMethodPrefix supported: " + inst.isNativeMethodPrefixSupported());

        Set<String> transformedClasses = new TreeSet<>();


        String transformerClassStr = getMainAttributeFromJar(bootstrapJar, "Transformer-Class");
        Class<?> transformerClass = null;
        try {
            transformerClass = Class.forName(transformerClassStr, true, ClassLoader.getSystemClassLoader());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        System.out.println("==> createTransformer " + agentArgs + " | " + inst);
        ClassFileTransformer transformer = null;
        try {
            transformer = (ClassFileTransformer) transformerClass.getDeclaredConstructor(String.class , Instrumentation.class , Set.class ).newInstance(
                        mainClassJvmStr, inst, transformedClasses
                );
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        System.out.println("<== createTransformer " );

        System.out.println("==> addTransformer " + agentArgs + " | " + inst);
        inst.addTransformer(transformer, true);
        System.out.println("<== addTransformer " );

            retransform3(inst, transformedClasses);

            classes = inst.getAllLoadedClasses();

            System.out.println("...");


    }

    private static void retransform3(Instrumentation inst, Set<String> transformedClasses) {
        Class[] allclasses = inst.getAllLoadedClasses();
        for (Class<?> clazz : allclasses) {
                        /*
                        try {
                            System.out.println("t retransformClasses: " + clazz + " -->");
                            inst.retransformClasses(clazz);
                            System.out.println("t retransformClasses: " + clazz + " <--");
                        } catch (UnmodifiableClassException e) {
                            // TODO Auto-generated catch block
                            System.out.println("t retransformClasses: " + clazz + " FAILED");
                            e.printStackTrace();
                        }
                        */
// ClassReader classReader = new ClassReader( getClassBytes(clazz) );
            String resource = clazz.getName().replace('.', '/') + ".class";
            if(   transformedClasses.contains( clazz.getName().replace('.', '/') )   ) {
                System.out.println("t noReRedefineClass: " + resource + " (className)");
                continue;
            }
            if(resource.equals("java/lang/Object.class") ) {//ist in vererbungshierarchie von main macht probleme bei redefine
                continue;
            }
            //if(resource.equals("java/lang/String.class") ) {//test
            //    continue;
            // }
            if(resource.equals("counter/Counter.class") ) {//ist in vererbungshierarchie von main macht probleme "no classdef found
                continue;
            }

            if( resource.equals("java/util/TreeMap.class") ) {//ist in vererbungshierarchie von main
                System.out.println("BREAK noReRedefineClass: " + resource + " (className)");
                System.out.println("BREAK noReRedefineClass: " + resource + " (className)");
            }

            try {
                inst.retransformClasses(clazz);
            } catch (UnmodifiableClassException e) {
                System.err.println("t reTRANSClass: " + clazz + " UnmodifiableClassException " + e.getMessage());
                continue;
            }

            System.out.println("t retransformClasses: " + clazz + " || " + resource + " -->");

                       /* REDEFINE
                       transformedClasses.add(resource);
                       byte[] newClassBytes;
                       try {
                            //return classfileBuffer;
                              newClassBytes = Transformer.transform2( getClassBytes(clazz) );
                            try {
                               inst.redefineClasses( new ClassDefinition(clazz, newClassBytes) );
                            } catch (Exception e) {
                               System.out.println("t redefineClass: " + clazz + " " + e.getMessage());
                            }
                       } catch (Throwable e) {
                               System.out.println("t !!! exception 4 " + className + " | " + e.getClass().getName() + " | " + e.getMessage());
                            //e.printStackTrace();
                            //System.out.println("<- trans e ");
}
                        */


        }
    }

    private static String getMainAttributeFromJar(String jarPath, String attributeName) {
        File jarFile = new File(jarPath);
        if (!jarFile.exists() || !jarFile.isFile()) {
            System.err.println("The specified file does not exist or is not a valid file.");
            return null;
        }
        return getMainAttributeFromJar(jarFile, attributeName);
    }

    private static String getMainAttributeFromJar(File jarFile, String attributeName) {
        try (JarInputStream jarStream = new JarInputStream(new FileInputStream(jarFile))) {
            Manifest manifest = jarStream.getManifest();
            if (manifest != null) {
                Attributes attributes = manifest.getMainAttributes();
                String mainClass = attributes.getValue(attributeName);
                if (mainClass != null) {
                    System.out.println("Main-Class: " + mainClass);
                } else {
                    System.out.println("No Main-Class attribute found in the manifest.");
                }
                return mainClass;
            } else {
                System.out.println("No manifest found in the JAR file.");
            }
        } catch (Exception e) {
            System.err.println("Error reading the JAR file: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static String getMainAttributeFromJar(JarFile jarFile, String attributeName) {
        Manifest manifest = null;
        try {
            manifest = jarFile.getManifest();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (manifest == null) return null;
        Attributes attrs = manifest.getMainAttributes();
        if (attrs == null) return null;
        return attrs.getValue(attributeName);
    }


    private static Map<String,String> parseAgentArgs(String agentArgs) {
        Map<String,String> map = new TreeMap<>();
        if (agentArgs == null || agentArgs.trim().isEmpty()) return map;

        String[] parts = agentArgs.split("[,;]");
        for (String rawPart : parts) {
            String part = rawPart.trim();
            if (part.isEmpty()) continue;

            String[] kv = part.split(":", 2);
            if (kv.length == 1) {
                map.put(kv[0].trim(), "true");
            } else {
                map.put(kv[0].trim(), kv[1].trim());
            }
        }
        return map;
    }


}