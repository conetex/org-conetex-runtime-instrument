package org.conetex.contract.runtime.instrument.metrics.cost;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
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

            @Override
            protected void onMethodEnter() {
                // count method entry
                mv.visitMethodInsn(INVOKESTATIC,
                        "org/conetex/contract/runtime/instrument/counter/MethodEntry",
                        "increment",
                        "()V",
                        false);
                // super.onMethodEnter() is a empty hook. we do not need to call it
            }

            @Override
            public void visitMethodInsn(int opcode, String owner, String name,
                                        String descriptor, boolean isInterface) {
                // count method call
                mv.visitMethodInsn(INVOKESTATIC,
                        "org/conetex/contract/runtime/instrument/counter/MethodCall",
                        "increment",
                        "()V",
                        false);
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
                        mv.visitMethodInsn(INVOKESTATIC,
                                "org/conetex/contract/runtime/instrument/counter/CompareInt",
                                "increment",
                                "()V",
                                false);
                        break;

                    // count compare for Object
                    case IFNULL: case IFNONNULL: // (object compare jumps)
                    case IF_ACMPEQ: case IF_ACMPNE:
                        mv.visitMethodInsn(INVOKESTATIC,
                                "org/conetex/contract/runtime/instrument/counter/CompareObject",
                                "increment",
                                "()V",
                                false);
                        break;

                    // count every other jump [if/else, switch, break, continue, for, while, do] / unconditional jumps [goto, jsr (Jump to SubRoutine)]
                    default:
                        mv.visitMethodInsn(INVOKESTATIC,
                                "org/conetex/contract/runtime/instrument/counter/Jump",
                                "increment",
                                "()V",
                                false);
                        break;
                }
                super.visitJumpInsn(opcode, label);
            }

            @Override
            public void visitVarInsn(int opcode, int var) {
                switch (opcode) {
                    // count Read vars (Load)
                    case ILOAD: case LLOAD: case FLOAD: case DLOAD: case ALOAD:
                        mv.visitMethodInsn(INVOKESTATIC,
                                "org/conetex/contract/runtime/instrument/counter/VariableLoad",
                                "increment",
                                "()V",
                                false);
                        break;

                    // count write vars (Store)
                    case ISTORE: case LSTORE: case FSTORE: case DSTORE: case ASTORE:
                        mv.visitMethodInsn(INVOKESTATIC,
                                "org/conetex/contract/runtime/instrument/counter/VariableStore",
                                "increment",
                                "()V",
                                false);
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
                        mv.visitMethodInsn(INVOKESTATIC,
                                "org/conetex/contract/runtime/instrument/counter/CompareLong",
                                "increment",
                                "()V",
                                false);
                        break;

                    // count addition substraction negation
                    case IADD: // TODO Error in debug mode
                               case ISUB: case LADD: case LSUB:
                    case FADD: case FSUB: case DADD: case DSUB:
                    case INEG: case LNEG: case FNEG: case DNEG:
                        mv.visitMethodInsn(INVOKESTATIC,
                                "org/conetex/contract/runtime/instrument/counter/ArithmeticAddSubNeg",
                                "increment", "()V", false);
                        break;

                    // count multiplication
                    case IMUL: case LMUL: case FMUL: case DMUL:
                        mv.visitMethodInsn(INVOKESTATIC,
                                "org/conetex/contract/runtime/instrument/counter/ArithmeticMul",
                                "increment", "()V", false);
                        break;

                    // count division / modulo
                    case IDIV: case IREM: case LDIV: case LREM:
                    case FDIV: case DDIV: case FREM: case DREM:
                        mv.visitMethodInsn(INVOKESTATIC,
                                "org/conetex/contract/runtime/instrument/counter/ArithmeticDivRem",
                                "increment", "()V", false);
                        break;

                    // count read array
                    case IALOAD: case LALOAD: case FALOAD: case DALOAD:
                    case AALOAD: case BALOAD: case CALOAD: case SALOAD:
                        mv.visitMethodInsn(INVOKESTATIC,
                                "org/conetex/contract/runtime/instrument/counter/ArrayLoad",
                                "increment",
                                "()V",
                                false);
                        break;

                    // count write array
                    case IASTORE: case LASTORE: case FASTORE: case DASTORE:
                    case AASTORE: case BASTORE: case CASTORE: case SASTORE:
                        mv.visitMethodInsn(INVOKESTATIC,
                                "org/conetex/contract/runtime/instrument/counter/ArrayStore",
                                "increment",
                                "()V",
                                false);
                        break;

                    // count monitor enter/exit
                    case MONITORENTER: case MONITOREXIT:
                        mv.visitMethodInsn(INVOKESTATIC,
                                "org/conetex/contract/runtime/instrument/counter/Monitor",
                                "increment", "()V", false);
                        break;

                    // count exception throw
                    case ATHROW:
                        mv.visitMethodInsn(INVOKESTATIC,
                                "org/conetex/contract/runtime/instrument/counter/ExceptionThrow",
                                "increment", "()V", false);
                        break;

                }
                super.visitInsn(opcode);
            }

            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                switch (opcode) {
                    case GETFIELD: case GETSTATIC:
                        mv.visitMethodInsn(INVOKESTATIC,
                                "org/conetex/contract/runtime/instrument/counter/FieldLoad",
                                "increment", "()V", false);
                        break;
                    case PUTFIELD: case PUTSTATIC:
                        mv.visitMethodInsn(INVOKESTATIC,
                                "org/conetex/contract/runtime/instrument/counter/FieldStore",
                                "increment", "()V", false);
                        break;
                }
                super.visitFieldInsn(opcode, owner, name, descriptor);
            }

            @Override
            public void visitTypeInsn(int opcode, String type) {
                if (opcode == ANEWARRAY) {
                    mv.visitMethodInsn(INVOKESTATIC,
                            "org/conetex/contract/runtime/instrument/counter/ArrayNew",
                            "increment",
                            "()V",
                            false);
                }
                else if (opcode == CHECKCAST || opcode == INSTANCEOF) {
                    mv.visitMethodInsn(INVOKESTATIC,
                            "org/conetex/contract/runtime/instrument/counter/TypeCheck",
                            "increment", "()V", false);
                }
                super.visitTypeInsn(opcode, type);
            }

            @Override
            public void visitIntInsn(int opcode, int operand) {
                if (opcode == NEWARRAY) {
                    mv.visitMethodInsn(INVOKESTATIC,
                            "org/conetex/contract/runtime/instrument/counter/ArrayNew",
                            "increment",
                            "()V",
                            false);
                }
                super.visitIntInsn(opcode, operand);
            }

            @Override
            public void visitMultiANewArrayInsn(String desc, int dims) {
                mv.visitMethodInsn(INVOKESTATIC,
                        "org/conetex/contract/runtime/instrument/counter/ArrayNew",
                        "increment",
                        "()V",
                        false);
                super.visitMultiANewArrayInsn(desc, dims);
            }

        };
    }
}