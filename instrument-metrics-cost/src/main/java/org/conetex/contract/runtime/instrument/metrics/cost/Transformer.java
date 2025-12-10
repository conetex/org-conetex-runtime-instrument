package org.conetex.contract.runtime.instrument.metrics.cost;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;

public class Transformer {
    public static byte[] transform2(byte[] classBytes) throws IOException {
        System.out.println(" classwriter->");
        ClassReader reader = new ClassReader(classBytes);
        //ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
        ClassVisitor visitor = new MethodCallCounter(writer);        
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        byte[] re = writer.toByteArray();
        System.out.println(" <-classwriter");
        return re;
    }
    
    public static byte[] norealtransform(byte[] classBytes) {
        //System.out.println("noclasswriter->");
        ClassReader reader = new ClassReader(classBytes);
        //ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
        byte[] re = writer.toByteArray();
        //System.out.println("<-noclasswriter");
        return re;
    }
    
    public static byte[] notransform(byte[] classBytes) {
        //System.out.println("noclasswriter->");
        byte[] re = classBytes;
        //System.out.println("<-noclasswriter");
        return re;
    }
    
}
