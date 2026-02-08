package org.conetex.runtime.instrument.agent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.*;

// org.conetex.contract.runtime is the right place
public class Instrument {

    public static final String ARG_PATHS_OF_INSTRUMENTATION_JARS = "pathToTransformerJar";

    static void applyX(String agentArgs, Instrumentation inst) {
        System.out.println("working here: " + new File(".").getAbsolutePath());
        Path agentPath;
        try {
            agentPath = Paths.get(Agent.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        Path agentDir = Files.isDirectory(agentPath) ? agentPath : agentPath.getParent();

        Map<String, String> args = parseAgentArgs(agentArgs);

        String bootstrapJarPath = args.get(ARG_PATHS_OF_INSTRUMENTATION_JARS);
        if (bootstrapJarPath == null || bootstrapJarPath.isEmpty()) {
            throw new IllegalArgumentException("premain Missing required agent argument: " + ARG_PATHS_OF_INSTRUMENTATION_JARS + ":<path-to-bootstrap-jar>");
        }
        Path bootstrapPath = agentDir.resolve(bootstrapJarPath);
        System.out.println("transformer: " + bootstrapPath);
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

        /* load classes of transformer before adding it to the instrumentation
        try {
            loadAllClassesFromJar(bootstrapPath.toFile(), Instrument.class.getClassLoader());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/

        String transformerClassStr = getMainAttributeFromJar(bootstrapJar, "coreClass");
        Class<?> transformerClass;
        try {
            transformerClass = Class.forName(transformerClassStr, true, inst.getClass().getClassLoader());
            //transformerClass = Class.forName(transformerClassStr);
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


        try {
            Method addToHandledClassesMethod = transformer.getClass().getMethod("addToHandledClasses", String.class);
            addToHandledClasses(bootstrapPath.toFile(), transformer, addToHandledClassesMethod);
        } catch (NoSuchMethodException | IOException e) {
            throw new RuntimeException("Failed to call addToHandledClassesMethod", e);
        }




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
        /*
        try {
            Method resetMethod = transformer.getClass().getMethod("resetCounters");
            resetMethod.invoke(transformer);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to call resetCounters", e);
        }
        */

        // add Shutdown-Hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown-Hook started.");
            try {
                try {
                    Method blockIncrementMethod = transformer.getClass().getMethod("blockIncrement", boolean.class);
                    blockIncrementMethod.invoke(transformer, true);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("Failed to call blockIncrement(false)", e);
                }
                try {
                    Method reportMethod = transformer.getClass().getMethod("report");
                    reportMethod.invoke(transformer);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("Failed to call report (" + e.getMessage() + ")", e);
                }
            } catch (Exception e) {
                System.err.println("ERROR " + e.getClass() + ": " + e.getMessage());
            }
            finally {
                System.out.println("Shutdown-Hook ended.");
            }
        }));

        System.out.println("premain end");

        try {
            Method blockIncrementMethod = transformer.getClass().getMethod("blockIncrement", boolean.class);
            blockIncrementMethod.invoke(transformer, false);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to call blockIncrement(false)", e);
        }

    }

    private static Class<?> xloadClassFromModule(String className, String moduleName) {
        try {
            // ModuleLayer Boot: Zugriff über aktuellen Layer
            ModuleLayer layer = ModuleLayer.boot();

            ClassLoader classLoader = layer.findLoader(moduleName);

            // Klasse laden
            Class<?> loadedClass = Class.forName(className, true, classLoader);
            System.out.println("Klasse geladen: " + loadedClass.getName());
            return loadedClass;
        } catch (Exception e) {
            System.err.println("Fehler beim Laden der Klasse: " + e.getMessage());
        }
        return null;
    }

    /**
     * Given a list of class names, find the module exporting the class's package,
     * and load the class using the module's ClassLoader.
     *
     * @param classNames List of fully qualified class names to load
     * @return A map of class names to their loaded Class objects, or null if not found
     */
    private static Map<String, Class<?>> loadClassesFromModules(List<String> classNames) {
        // Result: Map with class name as key and loaded Class<?> object as value
        Map<String, Class<?>> result = new TreeMap<>();

        // Access the Boot Layer of the Module System (base layer on JVM startup)
        ModuleLayer bootLayer = ModuleLayer.boot();

        // Iterate over all class names in the list
        for (String className : classNames) {
            try {
                // Extract the package name from the fully qualified class name
                String packageName = className.substring(0, className.lastIndexOf('.'));

                // Look for a module that exports the package containing the class
                Optional<Module> moduleOptional = bootLayer.modules().stream()
                        .filter(module -> module.getDescriptor().exports().stream()
                                .anyMatch(export -> export.source().equals(packageName)))
                        .findFirst();

                // If a module is found, load the class using its ClassLoader
                if (moduleOptional.isPresent()) {
                    Module module = moduleOptional.get();

                    // Use the class loader associated with the module
                    ClassLoader moduleClassLoader = bootLayer.findLoader(module.getName());
                    Class<?> loadedClass = Class.forName(className, true, moduleClassLoader);

                    System.out.println("loadedClass: '" + loadedClass + "', module: '" + loadedClass.getModule() + "', loader: '" + loadedClass.getClassLoader() + "'");

                    // Add the loaded class to the result map
                    result.put(className, loadedClass);
                } else {
                    // If no module is found, mark the class as null
                    result.put(className, null);
                    System.err.println("No module exports the package: " + packageName);
                }
            } catch (ClassNotFoundException e) {
                // Handle cases where the class cannot be found or loaded
                result.put(className, null);
                System.err.println("Error loading class " + className + ": " + e.getMessage());
            }
        }

        return result;
    }

    /*
    01.01:









     */


    static void apply(String agentArgs, Instrumentation inst) {

        /*
        // Zugriff auf den Boot-Layer des Modulsystems
        ModuleLayer bootLayer = ModuleLayer.boot();

        // Alle Module im Boot-Layer durchlaufen
        bootLayer.modules().forEach(module -> {
            ModuleDescriptor descriptor = module.getDescriptor();
            String moduleName = descriptor.name();
            if(moduleName.startsWith("org.conetex.runtime.instrument")){
                System.out.println("Modul erkannt: " + descriptor.name());
                // Prüfe, ob bestimmte Klassen oder Pakete im Modul enthalten sind
                descriptor.exports().stream()
                        .map(ModuleDescriptor.Exports::source)
                        .forEach(packageName -> System.out.println("Exportiertes Paket: " + packageName));
            }

        });
        */

        // infos on inst
        System.out.println("premain Redefine supported: " + inst.isRedefineClassesSupported());
        System.out.println("premain Retransform supported: " + inst.isRetransformClassesSupported());
        System.out.println("premain NativeMethodPrefix supported: " + inst.isNativeMethodPrefixSupported());

        System.out.println("inst.getClass() Module: " + inst.getClass().getModule() + " ClassLoader: " + inst.getClass().getClassLoader());
        System.out.println("Instrument.class Module: " + Instrument.class.getModule() + " ClassLoader: " + Instrument.class.getClassLoader());

        System.out.println("premain prepare transformer '" + agentArgs + "' | '" + inst + "'");

        File[] instrumentationFiles = findInstrumentationFilesFromAgentArgs(agentArgs);
        ClassFileTransformer transformer = null;
        for (File bootstrapFile : instrumentationFiles){
            // load class
            Class<?> transformerClass = loadCoreClass(
                    inst,
                    new ClassLoader[]{inst.getClass().getClassLoader(), Instrument.class.getClassLoader(), null},
                    bootstrapFile
                );
            if (ClassFileTransformer.class.isAssignableFrom(transformerClass)) {
                try {
                    // create transformer
                    transformer = (ClassFileTransformer) transformerClass.getDeclaredConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if(transformer == null){
            throw new RuntimeException("no transformer created");
        }

        // add classes to handled list
        try {
            Method addToHandledClassesMethod = transformer.getClass().getMethod("addToHandledClasses", String.class);
            for (File bootstrapFileToAddToHandledClasses : instrumentationFiles) {
                System.out.println("--> '" + bootstrapFileToAddToHandledClasses + "'");
                addToHandledClasses(bootstrapFileToAddToHandledClasses, transformer, addToHandledClassesMethod);
            }
        } catch (NoSuchMethodException | IOException e) {
            throw new RuntimeException("Failed to call addToHandledClassesMethod", e);
        }






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


        // DELETE ======= append is here
        /*String appendToBootstrapClassLoaderSearchStr = getMainAttributeFromJar(bootstrapJar, "appendToBootstrapClassLoaderSearch");
        assert appendToBootstrapClassLoaderSearchStr != null;
        if(! appendToBootstrapClassLoaderSearchStr.equals("false")){ // this is not needed if -Xbootclasspath/a:/path/to/bootstrapJar todo: can we find out at runtime?
            // TODO we do not want org.conetex.contract.runtime.instrument:* loaded before this:
            inst.appendToBootstrapClassLoaderSearch(bootstrapJar); // warning will not occur if -Xshare:off todo: can we find out at runtime?
            System.out.println("added jar to BootstrapClassLoaderSearch");
        }*/

        // is transformer loadable?

        /*try {
            transformerClass = Class.forName(transformerClassStr);
        } catch (ClassNotFoundException e) {
            System.out.println("1 can not load " + transformerClassStr + " ");
        }*/

        /*if(transformerClass == null){
            try {
                transformerClass = Class.forName(transformerClassStr, true, ClassLoader.getSystemClassLoader());
            } catch (ClassNotFoundException e) {
                System.out.println("3 can not load " + transformerClassStr + " ");
            }
        }*/

        /*if(transformerClass == null){
            try {
                transformerClass = Class.forName(transformerClassStr);
            } catch (ClassNotFoundException e) {
                System.out.println("1b can not load " + transformerClassStr + " ");
            }
        }*/

        /*if(transformerClass == null){
            try {
                transformerClass = Class.forName(transformerClassStr, true, ClassLoader.getSystemClassLoader());
            } catch (ClassNotFoundException e) {
                System.err.println("3b can not load " + transformerClassStr + " ");
                throw new RuntimeException(e);
            }
        }*/








        /*
        try {
            transformerClass = Class.forName(transformerClassStr, true, ClassLoader.getSystemClassLoader());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        */




        // call initMainClassJvmName( mainClassJvmStr );
        try {
            Method initMethod = transformer.getClass().getMethod("initMainClassJvmName", String.class);
            initMethod.invoke(transformer, mainClassJvmStr);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to call initMainClassJvmName", e);
        }
        System.out.println("premain createdTransformer " + transformer);



        // call resetCounters( )
        /*
        try {
            Method resetMethod = transformer.getClass().getMethod("resetCounters");
            resetMethod.invoke(transformer);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to call resetCounters", e);
        }
        */





        inst.addTransformer(transformer, true);
        System.out.println("premain transformer added");

        /* call triggerRetransform( inst, inst.getAllLoadedClasses() )
        try {
            Method retransformMethod = transformer.getClass().getMethod("triggerRetransform", Instrumentation.class, Class[].class);
            retransformMethod.invoke(transformer, inst, inst.getAllLoadedClasses());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to call triggerRetransform", e);
        }*/

        // add Shutdown-Hook
        /*
        ClassFileTransformer finalTransformer = transformer;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown-Hook started.");
            try {
                try {
                    Method blockIncrementMethod = finalTransformer.getClass().getMethod("blockIncrement", boolean.class);
                    blockIncrementMethod.invoke(finalTransformer, true);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("Failed to call blockIncrement(false)", e);
                }
                try {
                    Method reportMethod = finalTransformer.getClass().getMethod("report");
                    reportMethod.invoke(finalTransformer);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("Failed to call report (" + e.getMessage() + ")", e);
                }
            } catch (Exception e) {
                System.err.println("ERROR " + e.getClass() + ": " + e.getMessage());
            }
            finally {
                System.out.println("Shutdown-Hook ended.");
            }
        }));
        */

        try {
            Method blockIncrementMethod = transformer.getClass().getMethod("blockIncrement", boolean.class);
            blockIncrementMethod.invoke(transformer, false);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to call blockIncrement(false)", e);
        }

        System.out.println("premain end");

    }

    private static Class<?> loadCoreClass(Instrumentation inst,
                                          ClassLoader[] cls, File bootstrapFile) {

        JarFile bootstrapJar = fileToJarFile(bootstrapFile);

        List<String> allClassNamesToLoad = getAllClassNamesFromJar(bootstrapFile);

        // find coreClass name
        String coreClassName = getMainAttributeFromJar(bootstrapJar, "coreClass");
        System.out.println("coreClassStr: " + coreClassName);

        String appendToBootstrapStr = getMainAttributeFromJar(bootstrapJar, "appendToBootstrap");
        System.out.println("appendToBootstrap: " + appendToBootstrapStr);
        boolean appendToBootstrap = appendToBootstrapStr != null && appendToBootstrapStr.equals("true");

        // load coreClass - BootstrapClassLoader
        if(appendToBootstrap){
            // Bootstrap-ClassLoader
            inst.appendToBootstrapClassLoaderSearch(bootstrapJar); // warning will not occur if -Xshare:off
            Class<?> coreClassBootstrap = loadClass(coreClassName, null);
            if(coreClassBootstrap == null){
                throw new RuntimeException("can not load '" + coreClassName + "'");
            }
            // load classes of transformer before adding it to the instrumentation
            // otherwise we create transformation loops
            loadAllClassesFromJar(allClassNamesToLoad, null);
            return coreClassBootstrap;
        }

        // load coreClass - try module-path mode
        Map<String, Class<?>> loadedTransformerClasses = loadClassesFromModules(allClassNamesToLoad);
        Class<?> coreClass = loadedTransformerClasses.get(coreClassName);
        if(coreClass != null) {
            System.out.println("'" + coreClassName + "' was loaded in module mode by " + coreClass.getClassLoader());
            return coreClass;
        }

        // load coreClass - try classpath mode
        for(int i = 0; i < cls.length - 1; i++){

            // todo create run configs with nonfat-jar in classpath. then it is enough to just do "// normal-Classloaders" coreClass = loadClass(coreClassName, cls[i]);
            if(cls[i] == null){
                // Bootstrap-ClassLoader
                coreClass = loadClass(coreClassName, null);
                if(coreClass == null){
                    System.out.println("add '" + bootstrapFile + "' to BootstrapClassLoaderSearch. you may want to set -Xbootclasspath.");
                    inst.appendToBootstrapClassLoaderSearch(bootstrapJar); // warning will not occur if -Xshare:off
                    coreClass = loadClass(coreClassName, null);
                }
            }
            else{
                // normal-Classloaders
                coreClass = loadClass(coreClassName, cls[i]);
            }

            if(coreClass != null){
                // load classes of transformer before adding it to the instrumentation
                // otherwise we create transformation loops
                loadAllClassesFromJar(allClassNamesToLoad, cls[i]);
                return coreClass;
            }

        }

        throw new RuntimeException("can not load '" + coreClassName + "'");
    }

    private static Class<?> loadClass(String classStr, ClassLoader cls) {
        try {
            Class<?> transformerClass = Class.forName(classStr, true, cls);
            System.out.println("'" + classStr + "' was loaded by " + cls);
            return transformerClass;
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            System.err.println("'" + classStr + "' was not loaded by " + cls);
        }
        return null;
    }



    private static JarFile fileToJarFile(File bootstrapFile) {
        JarFile bootstrapJar;
        try {
            bootstrapJar = new JarFile(bootstrapFile);
        } catch (IOException e) {
            throw new RuntimeException("can not open jar file", e);
        }
        return bootstrapJar;
    }

    private static File[] findInstrumentationFilesFromAgentArgs(String agentArgs) {
        Map<String, String> args = parseAgentArgs(agentArgs);
        String bootstrapJarPathsStr = args.get(ARG_PATHS_OF_INSTRUMENTATION_JARS);
        if (bootstrapJarPathsStr == null || bootstrapJarPathsStr.isEmpty()) {
            throw new IllegalArgumentException("premain Missing required agent argument: " + ARG_PATHS_OF_INSTRUMENTATION_JARS + ":<paths-to-instrumentation-jars>");
        }

        String[] bootstrapJarPaths = bootstrapJarPathsStr.split(",");
        File[] re = new File[bootstrapJarPaths.length];
        for (int i = 0; i < bootstrapJarPaths.length; i++) {
            re[i] = findInstrumentationFile(bootstrapJarPaths[i]);
        }
        return re;
    }

    private static File findInstrumentationFile(String bootstrapJarPath) {

        File workingDir = (new File(".")).getAbsoluteFile();
        System.out.println("working dir: " + new File(".").getAbsoluteFile());
        Path  bootstrapPathRelativeToWorkingDir = workingDir.toPath().resolve(bootstrapJarPath);
        if(Files.exists(bootstrapPathRelativeToWorkingDir)){
            try {
                System.out.println("instrument jar: " + bootstrapPathRelativeToWorkingDir.toFile().getCanonicalFile());
                return bootstrapPathRelativeToWorkingDir.toFile().getCanonicalFile();
            } catch (IOException e) {
                System.err.println("can not find instrument jar: " + bootstrapPathRelativeToWorkingDir);
                throw new RuntimeException("can not find instrument jar " + e);
            }
        }

        Path agentPath;
        try {
            agentPath = Paths.get(Agent.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("can not find instrument jar " + e);
        }
        Path agentDir = Files.isDirectory(agentPath) && !agentPath.endsWith("classes") ? agentPath : agentPath.getParent();
        System.out.println("agent dir: " + agentDir);

        Path bootstrapPathRelativeToAgentDir = agentDir.resolve(bootstrapJarPath);
        if(Files.exists(bootstrapPathRelativeToAgentDir)){
            try {
                System.out.println("instrument jar: " + bootstrapPathRelativeToAgentDir.toFile().getCanonicalFile());
                return bootstrapPathRelativeToAgentDir.toFile().getCanonicalFile();
            } catch (IOException e) {
                System.err.println("can not find instrument jar: " + bootstrapPathRelativeToAgentDir);
                throw new RuntimeException("can not find instrument jar " + e);
            }
        }

        throw new RuntimeException("can not find transformer at '" + bootstrapPathRelativeToWorkingDir + "' or '" + bootstrapPathRelativeToAgentDir + "'");
    }

    public static void addToHandledClasses(File jarFile, ClassFileTransformer transformer, Method addToHandledClassesMethod) throws IOException {

        try (JarInputStream jar = new JarInputStream(new FileInputStream(jarFile))) {
            JarEntry entry;
            while ((entry = jar.getNextJarEntry()) != null) {
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName()
                            .replace(".class", "");
                    try {
                        addToHandledClassesMethod.invoke(transformer, className);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        System.out.println("can not addToHandledClassesMethod - " + className + " " + e.getMessage());
                    }
                    System.out.println("addToHandledClassesMethod - " + className);

                }
            }
        }
    }

    public static void xloadAllClassesFromJar(File jarFile, ClassLoader loader)  {
        try (JarInputStream jar = new JarInputStream(new FileInputStream(jarFile))) {
            JarEntry entry;
            while ((entry = jar.getNextJarEntry()) != null) {
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName()
                            .replace('/', '.')
                            .replace(".class", "");
                    try {
                        Class<?> c = Class.forName(className, true, loader);
                        System.out.println("load - " + className + " " + c.getModule().getName());
                    } catch (Throwable t) {
                        System.err.println("error at load - " + className + " " + t.getMessage());
                        // optional: ignorieren oder loggen
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("can not loadAllClassesFromJar", e);
        }
    }

    public static void loadAllClassesFromJar(List<String> classNames, ClassLoader loader)  {
        for (String className : classNames) {
            try {
                Class<?> c = Class.forName(className, true, loader);
                System.out.println("load - class: '" + className + "' module: '" + c.getModule().getName() + "' loader: '" + loader + "'");
            } catch (Throwable t) {
                System.err.println("error load - class: '" + className + "' loader: '" + loader + "' error: '" + t.getClass() + "' - '" + t.getMessage() + "'");
                throw new RuntimeException("error load - class: '" + className + "' loader: '" + loader + "'", t);
            }
        }
    }

    public static List<String> getAllClassNamesFromJar(File jarFile)  {
        List<String> result = new LinkedList<String>();
        try (JarInputStream jar = new JarInputStream(new FileInputStream(jarFile))) {
            JarEntry entry;
            while ((entry = jar.getNextJarEntry()) != null) {
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName()
                            .replace('/', '.')
                            .replace(".class", "");
                    result.add(className);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("can not getAllClassesNamesFromJar", e);
        }
        return result;
    }

    private static Map<String,String> parseAgentArgs(String agentArgs) {
        Map<String,String> map = new TreeMap<>();
        if (agentArgs == null || agentArgs.trim().isEmpty()) return map;

        String[] parts = agentArgs.split("[;]");
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
            throw new RuntimeException("getMainAttributeFromJar ", e);
        }
        if (manifest == null){
            throw new RuntimeException("manifest == null");
        }
        Attributes attrs = manifest.getMainAttributes();
        if (attrs == null) {
            return null;
        }
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


    public static void setupModules() throws Exception {
        // 1. ModuleFinder: Gibt vor, welche Module erkannt und aufgelöst werden sollen
        ModuleFinder appModuleFinder = ModuleFinder.of(
                Path.of("agent/target/agent-0.0.1-SNAPSHOT.jar"),
                Path.of("metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT-fat.jar"),
                Path.of("test/jar-module/target/jar-module-0.0.1-SNAPSHOT.jar")
        );

        // 2. ModuleFinder für Eltern-Schicht (Boot Layer)
        ModuleFinder systemModuleFinder = ModuleFinder.ofSystem();

        // 3. Erstelle die Configuration, welche nur die übergebenen Module verwendet
        // Eltern Layer: Bootstrap Layer
        Configuration bootLayerConfiguration = ModuleLayer.boot().configuration();

        Configuration appConfiguration = bootLayerConfiguration.resolveAndBind(
                appModuleFinder,
                ModuleFinder.of(),
                Set.of(
                        "org.conetex.runtime.instrument.agent",
                        "org.conetex.runtime.instrument.metrics.cost",
                        "org.conetex.runtime.instrument.test.jar.module"
                )
        );

        // 4. Erstelle einen neuen ClassLoader (für unsere App-Schicht)
        ClassLoader parentClassLoader = ClassLoader.getSystemClassLoader();
        ClassLoader appClassLoader = new ClassLoader(parentClassLoader) {};

        // 5. Neues ModuleLayer hinzufügen
        ModuleLayer appLayer = ModuleLayer.defineModules(
                appConfiguration,
                (java.util.List<ModuleLayer>) Set.of(ModuleLayer.boot()),
                name -> appClassLoader  // Zuordnung des Custom ClassLoaders
        ).layer();

        // 6. Zugriff auf geladenes Modul und Klassentyp (siehe Beispiel-Klasse Main)
        Class<?> mainClass = appLayer.findLoader("org.conetex.runtime.instrument.test.jar.module")
                .loadClass("org.conetex.runtime.instrument.test.jar.module.Main");

        // Methodenausführung: Hauptmethode
        Method mainMethod = mainClass.getDeclaredMethod("main", String[].class);
        String[] mainArgs = {};
        mainMethod.invoke(null, (Object) mainArgs);
    }

}
