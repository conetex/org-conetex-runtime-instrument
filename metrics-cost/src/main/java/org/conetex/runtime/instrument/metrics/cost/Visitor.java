package org.conetex.runtime.instrument.metrics.cost;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;

public class Visitor extends ClassVisitor {

    public Visitor(ClassVisitor cv) {
        super(Opcodes.ASM9, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

        return new AdviceAdapter(Opcodes.ASM9, mv, access, name, desc) {

            // todo dynamic mode does not work since we are running into loops
            private void visitMvDynamic(String incrementMethodEntry) {
                // Register an INVOKEDYNAMIC instruction here
                mv.visitInvokeDynamicInsn(
                        incrementMethodEntry,                                                      // Name of the method (dynamic name)
                        "()V",                                                                     // Method descriptor
                        new Handle(
                                Opcodes.H_INVOKESTATIC,                                            // Bootstrap method type (static method)
                                "org/conetex/runtime/instrument/metrics/cost/unnamed/Bootstrap",      // Owner class
                                "callSite",                                                       // Bootstrap method name (defined in Owner class)
                                "(" +
                                        "Ljava/lang/invoke/MethodHandles$Lookup;" +
                                        "Ljava/lang/String;" +
                                        "Ljava/lang/invoke/MethodType;" +
                                        //"Ljava/lang/Class;" +                                    // argument Real-Owner class
                                        ")" +
                                        "Ljava/lang/invoke/CallSite;",                             // Method descriptor
                                false                                                              // Whether this is an interface method
                        )
                        //, Type.getType("Lorg/conetex/runtime/instrument/metrics/cost/Counters;") // Pass the Real-Owner class as an argument
                );
            }

            private void visitMv(String incrementMethodEntry, int opcode){
                visitMvStatic(incrementMethodEntry);
            }

            private void visitMvStatic(String incrementMethodEntry) {
                mv.visitMethodInsn(INVOKESTATIC,
                        // for module- and cp-mode - call to the generic targets in bootstrap (generated from Counters):
                        "org/conetex/runtime/instrument/metrics/cost/unnamed/CounterMethods",
                        // for cp-mode only - call to the increment methods in Counters:
                        //"org/conetex/runtime/instrument/metrics/cost/Counters",
                        incrementMethodEntry,
                        "()V",
                        false);
            }

            private void visitMvStaticCountOpcodes(String incrementMethodEntry, int opcode) {
                // Push the string argument onto the stack
                mv.visitLdcInsn(incrementMethodEntry);
                // Push the opcode onto the stack
                mv.visitIntInsn(Opcodes.BIPUSH, opcode);

                // Pass the current opcode to the method being invoked
                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "org/conetex/runtime/instrument/metrics/cost/CountOpcodes",    // Owner class for method
                        "consume",                                                           // Target method name
                        "(Ljava/lang/String;I)V",                                                              // Method descriptor: accepts an int parameter
                        false                                                                // Is not for an interface
                );

            }

            @Override
            protected void onMethodEnter() {
                // count method entry
                visitMv("incrementMethodEntry", -256);
                // super.onMethodEnter() is a empty hook. we do not need to call it
            }

            @Override
            public void visitMethodInsn(int opcode, String owner, String name,
                                        String descriptor, boolean isInterface) {
                // count method call
                visitMv("incrementMethodCall", opcode);
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            }

            @Override
            public void visitJumpInsn(int opcode, Label label) {
                switch (opcode) {
                    // count compare for int, byte, short, char, boolean (conditional jumps)

                    case IFEQ: case IFNE: case IFLT: case IFGE:
                    case IFGT: case IFLE:
                    case IF_ICMPEQ: case IF_ICMPNE: case IF_ICMPLT: // (compare jumps)
                    case IF_ICMPGE: case IF_ICMPGT: case IF_ICMPLE:
                        visitMv("incrementCompareInt", opcode);
                        break;

                    // count compare for Object
                    case IFNULL: case IFNONNULL: // (object compare jumps)
                    case IF_ACMPEQ: case IF_ACMPNE:
                        visitMv("incrementCompareObject", opcode);
                        break;

                    // count every other jump [if/else, switch, break, continue, for, while, do] / unconditional jumps [goto, jsr (Jump to SubRoutine)]
                    default:
                        visitMv("incrementJump", opcode);
                        break;
                }
                super.visitJumpInsn(opcode, label);
            }

            @Override
            public void visitVarInsn(int opcode, int var) {
                switch (opcode) {
                    // count Read vars (Load)
                    case ILOAD: case LLOAD: case FLOAD: case DLOAD: case ALOAD:
                        visitMv("incrementVariableLoad", opcode);
                        break;

                    // count write vars (Store)
                    case ISTORE: case LSTORE: case FSTORE: case DSTORE: case ASTORE:
                        visitMv("incrementVariableStore", opcode);
                        break;
                }
                super.visitVarInsn(opcode, var);
            }

            @Override
            public void visitInsn(int opcode) {
                switch (opcode) {
                    // count compare for long, float, double
                    case LCMP:
                    case FCMPG: case FCMPL:
                    case DCMPG: case DCMPL:
                        visitMv("incrementCompareLong", opcode);
                        break;

                    // count addition substraction negation
                    case IADD: // TODO Error in debug mode
                               case ISUB: case LADD: case LSUB:
                    case FADD: case FSUB: case DADD: case DSUB:
                    case INEG: case LNEG: case FNEG: case DNEG:
                        visitMv("incrementArithmeticAddSubNeg", opcode);
                        break;

                    // count multiplication
                    case IMUL: case LMUL: case FMUL: case DMUL:
                        visitMv("incrementArithmeticMul", opcode);
                        break;

                    // count division / modulo
                    case IDIV: case IREM: case LDIV: case LREM:
                    case FDIV: case DDIV: case FREM: case DREM:
                        visitMv("incrementArithmeticDivRem", opcode);
                        break;

                    // count read array
                    case IALOAD: case LALOAD: case FALOAD: case DALOAD:
                    case AALOAD: case BALOAD: case CALOAD: case SALOAD:
                        visitMv("incrementArrayLoad", opcode);
                        break;

                    // count write array
                    case IASTORE: case LASTORE: case FASTORE: case DASTORE:
                    case AASTORE: case BASTORE: case CASTORE: case SASTORE:
                        visitMv("incrementArrayStore", opcode);
                        break;

                    // count monitor enter/exit
                    case MONITORENTER: case MONITOREXIT:
                        visitMv("incrementMonitor", opcode);
                        break;

                    // count exception throw
                    case ATHROW:
                        visitMv("incrementExceptionThrow", opcode);
                        break;

                }
                super.visitInsn(opcode);
            }

            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                switch (opcode) {
                    case GETFIELD: case GETSTATIC:
                        visitMv("incrementFieldLoad", opcode);
                        break;
                    case PUTFIELD: case PUTSTATIC:
                        visitMv("incrementFieldStore", opcode);
                        break;
                }
                super.visitFieldInsn(opcode, owner, name, descriptor);
            }

            @Override
            public void visitTypeInsn(int opcode, String type) {
                if (opcode == ANEWARRAY) {
                    visitMv("incrementArrayNew", opcode);
                }
                else if (opcode == CHECKCAST || opcode == INSTANCEOF) {
                    visitMv("incrementTypeCheck", opcode);
                }
                super.visitTypeInsn(opcode, type);
            }

            @Override
            public void visitIntInsn(int opcode, int operand) {
                if (opcode == NEWARRAY) {
                    visitMv("incrementArrayNew", opcode);
                }
                super.visitIntInsn(opcode, operand);
            }

            @Override
            public void visitMultiANewArrayInsn(String desc, int dims) {
                visitMv("incrementArrayNew", -255);
                super.visitMultiANewArrayInsn(desc, dims);
            }

        };
    }
}