# Gewichtungsmatrix für Counter-Klassen

Diese Matrix beschreibt die CPU- und Memory-Gewichte für jeden Counter basierend auf den zugeordneten Opcodes. Die Werte für CPU- und Memory-Gewicht sind heuristisch und spiegeln die relative Kostenstruktur einiger JVM-Bytecode-Befehle wider.

| Counter              | CPU-Gewicht | Memory-Gewicht | Zugeordnete Opcodes                                                                 |
|----------------------|-------------|----------------|-------------------------------------------------------------------------------------|
| ArithmeticAddSubNeg  | 0.2         | 0.1            | IADD, ISUB, INEG, LADD, LSUB, FADD, FSUB, DADD, DSUB, LNEG, FNEG, DNEG             |
| ArithmeticDivRem     | 0.7         | 0.1            | IDIV, IREM, LDIV, LREM, FDIV, DDIV, FREM, DREM                                     |
| ArithmeticMul        | 0.5         | 0.1            | IMUL, LMUL, FMUL, DMUL                                                             |
| ArrayLoad            | 0.4         | 0.6            | IALOAD, LALOAD, FALOAD, DALOAD, AALOAD, BALOAD, CALOAD, SALOAD                     |
| ArrayStore           | 0.4         | 0.6            | IASTORE, LASTORE, FASTORE, DASTORE, AASTORE, BASTORE, CASTORE, SASTORE             |
| ArrayNew             | 0.8         | 0.9            | NEWARRAY, ANEWARRAY, MULTIANEWARRAY                                                |
| FieldLoad            | 0.3         | 0.4            | GETFIELD, GETSTATIC                                                                |
| FieldStore           | 0.3         | 0.4            | PUTFIELD, PUTSTATIC                                                                |
| VariableLoad         | 0.2         | 0.3            | ILOAD, LLOAD, FLOAD, DLOAD, ALOAD                                                  |
| VariableStore        | 0.2         | 0.3            | ISTORE, LSTORE, FSTORE, DSTORE, ASTORE                                             |
| CompareInt           | 0.5         | 0.1            | IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, etc.|
| CompareLong          | 0.6         | 0.1            | LCMP                                                                               |
| CompareObject        | 0.5         | 0.1            | IFNULL, IFNONNULL, IF_ACMPEQ, IF_ACMPNE                                            |
| Jump                 | 0.3         | 0.15           | GOTO, JSR, IFEQ, IFNE, IFLT, IFLE, IFGT, etc.                                      |
| MethodCall           | 0.7         | 0.5            | INVOKEVIRTUAL, INVOKESTATIC, INVOKEINTERFACE                                       |
| MethodEntry          | 0.5         | 0.1            | (Kein spezifischer Opcode, Einstiegspunkt für Methoden)                            |
| ExceptionThrow       | 0.9         | 0.6            | ATHROW                                                                             |
| Monitor              | 0.6         | 0.5            | MONITORENTER, MONITOREXIT                                                          |
| TypeCheck            | 0.4         | 0.2            | CHECKCAST, INSTANCEOF                                                              |
