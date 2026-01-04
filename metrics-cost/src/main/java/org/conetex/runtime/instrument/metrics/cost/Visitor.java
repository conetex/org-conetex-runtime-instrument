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

            private void visitMvDyn(String incrementMethodEntry) {
                // Register an INVOKEDYNAMIC instruction here
                mv.visitInvokeDynamicInsn(
                    incrementMethodEntry,                                     // Name of the method (dynamic name)
                    "()V",                                                    // Method descriptor
                    new Handle(
                        Opcodes.H_INVOKESTATIC,                               // Bootstrap method type (static method)
                        "org/conetex/runtime/instrument/metrics/cost/bootstrap/Bootstrap", // Owner class
                        "bootstrap",                                          // Bootstrap method name (defined in Owner class)
                        "(" +
                                "Ljava/lang/invoke/MethodHandles$Lookup;" +
                                "Ljava/lang/String;" +
                                "Ljava/lang/invoke/MethodType;" +
                                "Ljava/lang/Class;" +
                        ")" +
                        "Ljava/lang/invoke/CallSite;",                        // Method descriptor
                        false                                                 // Whether this is an interface method
                    ),
                    Type.getType("Lorg/conetex/runtime/instrument/metrics/cost/Counters;") // Pass the Real-Owner class as an argument
                );

            }

            private void visitMv(String incrementMethodEntry){
                visitMvDyn(incrementMethodEntry);
            }

            private void visitMvStat(String incrementMethodEntry) {
                mv.visitMethodInsn(INVOKESTATIC,
                        "org/conetex/runtime/instrument/metrics/cost/Counters",
                        incrementMethodEntry,
                        "()V",
                        false);

            }

            //@Override
            protected void onMethodEnter() {
                // count method entry
                visitMv("incrementMethodEntry");
                // super.onMethodEnter() is a empty hook. we do not need to call it
            }

            //@Override
            public void visitMethodInsn(int opcode, String owner, String name,
                                        String descriptor, boolean isInterface) {
                // count method call
                visitMv("incrementMethodCall");
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            }

            //@Override
            public void visitJumpInsn(int opcode, Label label) {
                switch (opcode) {
                    // count compare for int, byte, short, char, boolean (conditional jumps)

                    case IFEQ: case IFNE: case IFLT: case IFGE:
                    case IFGT: case IFLE:
                    case IF_ICMPEQ: case IF_ICMPNE: case IF_ICMPLT: // (compare jumps)
                    case IF_ICMPGE: case IF_ICMPGT: case IF_ICMPLE:
                        visitMv("incrementCompareInt");
                        break;

                    // count compare for Object
                    case IFNULL: case IFNONNULL: // (object compare jumps)
                    case IF_ACMPEQ: case IF_ACMPNE:
                        visitMv("incrementCompareObject");
                        break;

                    // count every other jump [if/else, switch, break, continue, for, while, do] / unconditional jumps [goto, jsr (Jump to SubRoutine)]
                    default:
                        visitMv("incrementJump");
                        break;
                }
                super.visitJumpInsn(opcode, label);
            }

            //@Override
            public void visitVarInsn(int opcode, int var) {
                switch (opcode) {
                    // count Read vars (Load)
                    case ILOAD: case LLOAD: case FLOAD: case DLOAD: case ALOAD:
                        visitMv("incrementVariableLoad");
                        break;

                    // count write vars (Store)
                    case ISTORE: case LSTORE: case FSTORE: case DSTORE: case ASTORE:
                        visitMv("incrementVariableStore");
                        break;
                }
                super.visitVarInsn(opcode, var);
            }

            //@Override
            public void visitInsn(int opcode) {
                switch (opcode) {
                    // count compare for long, float, double
                    case LCMP:
                    case FCMPG: case FCMPL:
                    case DCMPG: case DCMPL:
                        visitMv("incrementCompareLong");
                        break;

                    // count addition substraction negation
                    case IADD: // TODO Error in debug mode
                               case ISUB: case LADD: case LSUB:
                    case FADD: case FSUB: case DADD: case DSUB:
                    case INEG: case LNEG: case FNEG: case DNEG:
                        visitMv("incrementArithmeticAddSubNeg");
                        break;

                    // count multiplication
                    case IMUL: case LMUL: case FMUL: case DMUL:
                        visitMv("incrementArithmeticMul");
                        break;

                    // count division / modulo
                    case IDIV: case IREM: case LDIV: case LREM:
                    case FDIV: case DDIV: case FREM: case DREM:
                        visitMv("incrementArithmeticDivRem");
                        break;

                    // count read array
                    case IALOAD: case LALOAD: case FALOAD: case DALOAD:
                    case AALOAD: case BALOAD: case CALOAD: case SALOAD:
                        visitMv("incrementArrayLoad");
                        break;

                    // count write array
                    case IASTORE: case LASTORE: case FASTORE: case DASTORE:
                    case AASTORE: case BASTORE: case CASTORE: case SASTORE:
                        visitMv("incrementArrayStore");
                        break;

                    // count monitor enter/exit
                    case MONITORENTER: case MONITOREXIT:
                        visitMv("incrementMonitor");
                        break;

                    // count exception throw
                    case ATHROW:
                        visitMv("incrementExceptionThrow");
                        break;

                }
                super.visitInsn(opcode);
            }

            //@Override
            public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                switch (opcode) {
                    case GETFIELD: case GETSTATIC:
                        visitMv("incrementFieldLoad");
                        break;
                    case PUTFIELD: case PUTSTATIC:
                        visitMv("incrementFieldStore");
                        break;
                }
                super.visitFieldInsn(opcode, owner, name, descriptor);
            }

            //@Override
            public void visitTypeInsn(int opcode, String type) {
                if (opcode == ANEWARRAY) {
                    visitMv("incrementArrayNew");
                }
                else if (opcode == CHECKCAST || opcode == INSTANCEOF) {
                    visitMv("incrementTypeCheck");
                }
                super.visitTypeInsn(opcode, type);
            }

            //@Override
            public void visitIntInsn(int opcode, int operand) {
                if (opcode == NEWARRAY) {
                    visitMv("incrementArrayNew");
                }
                super.visitIntInsn(opcode, operand);
            }

            //@Override
            public void visitMultiANewArrayInsn(String desc, int dims) {
                visitMv("incrementArrayNew");
                super.visitMultiANewArrayInsn(desc, dims);
            }

        };
    }
}