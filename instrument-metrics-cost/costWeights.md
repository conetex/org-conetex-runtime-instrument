# Weighting matrix for counters

This matrix describes the CPU and memory weights for each counter based on the assigned opcodes.
The values for CPU and memory weight are heuristic and reflect the relative cost structure of some JVM bytecode instructions.

| Counter                     | CPU rel* | CPU weight* | Mem rel* | Mem weight* | Assigned Opcodes                                                                                     |
|-----------------------------|----------|-------------|----------|-------------|------------------------------------------------------------------------------------------------------|
| ArithmeticAddSubNeg         | 1.6      | 0.035165    | 1.2      | 0.029197    | IADD, ISUB, INEG, LADD, LSUB, FADD, FSUB, DADD, DSUB, LNEG, FNEG, DNEG                               |
| ArithmeticDivRem            | 5        | 0.109890    | 1.8      | 0.043796    | IDIV, IREM, LDIV, LREM, FDIV, DDIV, FREM, DREM                                                       |
| ArithmeticMul               | 2        | 0.043956    | 1.4      | 0.034063    | IMUL, LMUL, FMUL, DMUL                                                                               |
| ArrayLoad                   | 2.2      | 0.048352    | 2.5      | 0.060827    | IALOAD, LALOAD, FALOAD, DALOAD, AALOAD, BALOAD, CALOAD, SALOAD                                       |
| ArrayNew                    | 4.5      | 0.098901    | 5        | 0.121654    | NEWARRAY, ANEWARRAY, MULTIANEWARRAY                                                                  |
| ArrayStore                  | 3        | 0.065934    | 3.5      | 0.085158    | IASTORE, LASTORE, FASTORE, DASTORE, AASTORE, BASTORE, CASTORE, SASTORE                               |
| CompareInt                  | 1.4      | 0.030769    | 1.2      | 0.029197    | IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE |
| CompareLong                 | 1.8      | 0.039560    | 1.5      | 0.036496    | LCMP                                                                                                 |
| CompareObject               | 2        | 0.043956    | 1.8      | 0.043796    | IFNULL, IFNONNULL, IF_ACMPEQ, IF_ACMPNE                                                              |
| ExceptionThrow              | 5        | 0.109890    | 4        | 0.097324    | ATHROW                                                                                               |
| FieldLoad                   | 2.4      | 0.052747    | 2.5      | 0.060827    | GETFIELD, GETSTATIC                                                                                  |
| FieldStore                  | 2.8      | 0.061539    | 3        | 0.072993    | PUTFIELD, PUTSTATIC                                                                                  |
| Jump                        | 1.3      | 0.028571    | 1        | 0.024331    | GOTO, JSR, IFEQ, IFNE, IFLT, IFLE, IFGT, etc.                                                        |
| MethodCall                  | 2.5      | 0.054945    | 2        | 0.048662    | INVOKEVIRTUAL, INVOKESTATIC, INVOKEINTERFACE                                                         |
| MethodEntry                 |          | 0.000000    |          | 0.000000    | (No opcode. This counter compensates for potential weaknesses in the instrumentation of MethodCall.) |
| Monitor                     | 3.5      | 0.076923    | 4        | 0.097324    | MONITORENTER, MONITOREXIT                                                                            |
| TypeCheck                   | 2.3      | 0.050550    | 2.5      | 0.060827    | CHECKCAST, INSTANCEOF                                                                                |
| VariableLoad (**BASELINE**) | 1        | 0.021978    | 1        | 0.024331    | ILOAD, LLOAD, FLOAD, DLOAD, ALOAD                                                                    |
| VariableStore               | 1.2      | 0.026374    | 1.2      | 0.029197    | ISTORE, LSTORE, FSTORE, DSTORE, ASTORE                                                               |
| **SUM**                     | **45.5** | **1**       | **41.1** | **1**       |                                                                                                      |

* **rel** means FACTOR relative to the **BASELINE** (1). it is used as relativ weight.
* **weight** is normalized. The relativ weights (rel) are each divided by the sum of all relativ weights (**45.5** and **41.1**). As a result, the sum of all normalized weights becomes **1**.