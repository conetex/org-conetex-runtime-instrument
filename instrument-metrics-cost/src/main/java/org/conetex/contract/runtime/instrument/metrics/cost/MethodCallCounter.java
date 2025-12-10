package org.conetex.contract.runtime.instrument.metrics.cost;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

public class MethodCallCounter extends ClassVisitor{
	
    public MethodCallCounter(ClassVisitor cv) {
        super(Opcodes.ASM9, cv);
        System.out.println("  init MethodCallCounter " + Counter.count);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        //System.out.println("  visitMethod-> " + access + " (access) | " + name + " (name) | " + desc + " (desc) | " + signature + " (signature) | " + (exceptions == null ? 0 : exceptions.length) + " (exceptions)");
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        AdviceAdapter re = new AdviceAdapter(Opcodes.ASM9, mv, access, name, desc) {
            @Override
            protected void onMethodEnter() {
            	//System.out.println("  onMethodEnter->");
                // Inject: Counter.count++;
                    mv.visitFieldInsn(GETSTATIC, "org/conetex/contract/runtime/instrument/metrics/cost/Counter", "count", "J");
                    mv.visitLdcInsn(1L);
                    mv.visitInsn(LADD);
                    mv.visitFieldInsn(PUTSTATIC, "org/conetex/contract/runtime/instrument/metrics/cost/Counter", "count", "J");
            	//System.out.println("  <-onMethodEnter");
            }
        };
        //System.out.println("  <-visitMethod");
        return re;
    }
    
}
