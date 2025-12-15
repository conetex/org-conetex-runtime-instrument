package org.conetex.contract.runtime;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

public class Instrument {

    public static final String ARG_PATH_TO_TRANSFORMER_JAR = "pathToTransformerJar";

    static void apply(String agentArgs, Instrumentation inst) {
        System.out.println("working here: " + new File(".").getAbsolutePath());
        Path agentPath;
        try {
            agentPath = Paths.get(Agent.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        Path agentDir = Files.isDirectory(agentPath) ? agentPath : agentPath.getParent();

        Map<String, String> args = parseAgentArgs(agentArgs);

        String bootstrapJarPath = args.get(ARG_PATH_TO_TRANSFORMER_JAR);
        if (bootstrapJarPath == null || bootstrapJarPath.isEmpty()) {
            throw new IllegalArgumentException("premain Missing required agent argument: " + ARG_PATH_TO_TRANSFORMER_JAR + ":<path-to-bootstrap-jar>");
        }
        Path bootstrapPath = agentDir.resolve(bootstrapJarPath);
        JarFile bootstrapJar;
        try {
            bootstrapJar = new JarFile(bootstrapPath.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String appendToBootstrapClassLoaderSearchStr = getMainAttributeFromJar(bootstrapJar, "appendToBootstrapClassLoaderSearch");
        assert appendToBootstrapClassLoaderSearchStr != null;
        if(! appendToBootstrapClassLoaderSearchStr.equals("false")){
            // TODO we do not want org.conetex.contract.runtime.instrument:* loaded before this:
            inst.appendToBootstrapClassLoaderSearch(bootstrapJar);
      }

        System.out.println("premain Redefine supported: " + inst.isRedefineClassesSupported());
        System.out.println("premain Retransform supported: " + inst.isRetransformClassesSupported());
        System.out.println("premain NativeMethodPrefix supported: " + inst.isNativeMethodPrefixSupported());

        String command = System.getProperty("sun.java.command");
        System.out.println("premain sun.java.command: " + command);
        assert command != null;

        String mainClassJavaStr;
        if(command.endsWith(".jar")) {
            // TODO we do not want org.conetex.contract.runtime.instrument:* in jar to run
            mainClassJavaStr = getMainAttributeFromJar(command, "Main-Class");
            System.out.println("premain mainClassStr of jar from sun.java.command: " + mainClassJavaStr);
            System.out.println("premain Build-Jdk-Spec of jar from sun.java.command: " + getMainAttributeFromJar(command, "Build-Jdk-Spec"));
        }
        else {
            mainClassJavaStr = command;
            System.out.println("premain mainClassStr equals sun.java.command");
            System.out.println("premain Build-Jdk-Spec unknown, because command does not end with jar");
        }
        assert mainClassJavaStr != null;
        String mainClassJvmStr = mainClassJavaStr.replace('.', '/');
        System.out.println("premain mainClassJvmStr from sun.java.command: " + mainClassJvmStr);

        Class<?>[] classes = inst.getAllLoadedClasses();
        System.out.println("premain allLoadedClasses size: " + classes.length);

        String transformerClassStr = getMainAttributeFromJar(bootstrapJar, "Transformer-Class");
        Class<?> transformerClass;
        try {
            transformerClass = Class.forName(transformerClassStr, true, ClassLoader.getSystemClassLoader());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        System.out.println("premain prepare transformer '" + agentArgs + "' | '" + inst + "'");
        ClassFileTransformer transformer;
        try {
            transformer = (ClassFileTransformer) transformerClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        // call initMainClassJvmName( mainClassJvmStr );
        try {
            Method initMethod = transformer.getClass().getMethod("initMainClassJvmName", String.class);
            initMethod.invoke(transformer, mainClassJvmStr);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to call initMainClassJvmName", e);
        }
        System.out.println("premain createdTransformer " + transformer);

        inst.addTransformer(transformer, true);
        System.out.println("premain transformer added");

        // call triggerRetransform( inst, inst.getAllLoadedClasses() )
        try {
            Method retransformMethod = transformer.getClass().getMethod("triggerRetransform", Instrumentation.class, Class[].class);
            retransformMethod.invoke(transformer, inst, inst.getAllLoadedClasses());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to call triggerRetransform", e);
        }

        // call resetCounters( )
        try {
            Method resetMethod = transformer.getClass().getMethod("resetCounters");
            resetMethod.invoke(transformer);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to call resetCounters", e);
        }

        System.out.println("premain end");

        // add Shutdown-Hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                try {
                    Method reportMethod = transformer.getClass().getMethod("report");
                    reportMethod.invoke(transformer);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("Failed to call report", e);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

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

    private static String getMainAttributeFromJar(File jarFile, String attributeName) {
        try (JarInputStream jarStream = new JarInputStream(new FileInputStream(jarFile))) {
            Manifest manifest = jarStream.getManifest();
            if (manifest != null) {
                Attributes attributes = manifest.getMainAttributes();
                String mainClass = attributes.getValue(attributeName);
                if (mainClass != null) {
                    System.out.println("premain Main-Class: " + mainClass);
                } else {
                    System.out.println("premain No Main-Class attribute found in the manifest.");
                }
                return mainClass;
            } else {
                System.out.println("premain No manifest found in the JAR file.");
            }
        } catch (Exception e) {
            System.err.println("premain Error reading the JAR file: " + e.getMessage());
        }
        return null;
    }

    public static String getMainAttributeFromJar(JarFile jarFile, String attributeName) {
        Manifest manifest;
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

    private static String getMainAttributeFromJar(String jarPath, String attributeName) {
        File jarFile = new File(jarPath);
        if (!jarFile.exists() || !jarFile.isFile()) {
            System.err.println("premain The specified file does not exist or is not a valid file.");
            return null;
        }
        return getMainAttributeFromJar(jarFile, attributeName);
    }

}
