# Gewichtungsmatrix für Counter-Klassen

Die folgende Gewichtungsmatrix beschreibt die CPU- und Memory-Gewichte für jeden Counter basierend auf den assoziierten Opcodes und ihren Kosten:

| Counter              | CPU-Gewicht | Memory-Gewicht | Opcodes                                                               |
|----------------------|-------------|----------------|-----------------------------------------------------------------------|
| ArithmeticAddSubNeg  | 0.2         | 0.1            | IADD, ISUB, INEG, LADD, LSUB, LNEG                                   |
| ArithmeticDivRem     | 0.7         | 0.1            | IDIV, IREM, LDIV, LREM                                               |
| ArithmeticMul        | 0.5         | 0.1            | IMUL, LMUL                                                           |
| ArrayLoad            | 0.4         | 0.6            | IALOAD, LALOAD, FALOAD, DALOAD, etc.                                 |
| ArrayStore           | 0.4         | 0.6            | IASTORE, LASTORE, FASTORE, DASTORE, etc.                             |
| ArrayNew             | 0.8         | 0.9            | NEWARRAY, ANEWARRAY, MULTIANEWARRAY                                  |
| FieldLoad            | 0.3         | 0.4            | GETFIELD, GETSTATIC                                                  |
| FieldStore           | 0.3         | 0.4            | PUTFIELD, PUTSTATIC                                                  |
| VariableLoad         | 0.2         | 0.3            | ILOAD, LLOAD, FLOAD, DLOAD                                           |
| VariableStore        | 0.2         | 0.3            | ISTORE, LSTORE, FSTORE, DSTORE                                       |
| CompareInt           | 0.5         | 0.1            | IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, etc.                                |
| CompareLong          | 0.5         | 0.1            | LCMP                                                                 |
| CompareObject        | 0.5         | 0.1            | IF_ACMPEQ, IF_ACMPNE                                                 |
| Jump                 | 0.3         | 0.15           | GOTO, JSR, IFEQ, IFNE, etc.                                          |
| MethodCall           | 0.7         | 0.5            | INVOKEVIRTUAL, INVOKESTATIC, INVOKEINTERFACE                         |
| MethodEntry          | 0.7         | 0.5            | (Method-Einstieg, keine spezifischen Opcodes nötig)                  |
| ExceptionThrow       | 0.9         | 0.6            | ATHROW                                                               |
| Monitor              | 0.6         | 0.5            | MONITORENTER, MONITOREXIT                                            |
| TypeCheck            | 0.4         | 0.2            | CHECKCAST, INSTANCEOF                                                |