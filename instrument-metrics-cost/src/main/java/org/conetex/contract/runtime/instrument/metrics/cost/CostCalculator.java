package org.conetex.contract.runtime.instrument.metrics.cost;

import org.conetex.contract.runtime.instrument.counter.*;

public class CostCalculator {

    // Gewichte für die einzelnen Counter
    private static final int WEIGHT_METHOD_CALL = 5;
    private static final int WEIGHT_METHOD_ENTRY = 5;
    private static final int WEIGHT_JUMP = 2;
    private static final int WEIGHT_COMPARE_INT = 1;
    private static final int WEIGHT_COMPARE_OBJECT = 1;
    private static final int WEIGHT_COMPARE_LONG = 1;
    private static final int WEIGHT_VARIABLE_LOAD = 1;
    private static final int WEIGHT_VARIABLE_STORE = 1;
    private static final int WEIGHT_ARITH_ADD_SUB_NEG = 1;
    private static final int WEIGHT_ARITH_MUL = 2;
    private static final int WEIGHT_ARITH_DIV_REM = 5;
    private static final int WEIGHT_ARRAY_LOAD = 2;
    private static final int WEIGHT_ARRAY_STORE = 2;
    private static final int WEIGHT_ARRAY_NEW = 8;

    /**
     * Hilfsmethode: summiert alle Werte einer verketteten Counter-Liste.
     */
    private static long sumCounterList(Object head) {
        long sum = 0;
        try {
            // Reflection, da alle Counter gleich aufgebaut sind
            Object current = head;
            while (current != null) {
                long count = (long) current.getClass().getMethod("getCount").invoke(current);
                sum += count;
                current = current.getClass().getMethod("getPrevious").invoke(current);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sum;
    }

    /**
     * Berechnet die Gesamtkostenmetrik.
     */
    public static long calculateTotalCost() {
        long total = 0;

        total += sumCounterList(MethodCall.getHead()) * WEIGHT_METHOD_CALL;
        total += sumCounterList(MethodEntry.getHead()) * WEIGHT_METHOD_ENTRY;
        total += sumCounterList(Jump.getHead()) * WEIGHT_JUMP;
        total += sumCounterList(CompareInt.getHead()) * WEIGHT_COMPARE_INT;
        total += sumCounterList(CompareObject.getHead()) * WEIGHT_COMPARE_OBJECT;
        total += sumCounterList(CompareLong.getHead()) * WEIGHT_COMPARE_LONG;
        total += sumCounterList(VariableLoad.getHead()) * WEIGHT_VARIABLE_LOAD;
        total += sumCounterList(VariableStore.getHead()) * WEIGHT_VARIABLE_STORE;
        total += sumCounterList(ArithmeticAddSubNeg.getHead()) * WEIGHT_ARITH_ADD_SUB_NEG;
        total += sumCounterList(ArithmeticMul.getHead()) * WEIGHT_ARITH_MUL;
        total += sumCounterList(ArithmeticDivRem.getHead()) * WEIGHT_ARITH_DIV_REM;
        total += sumCounterList(ArrayLoad.getHead()) * WEIGHT_ARRAY_LOAD;
        total += sumCounterList(ArrayStore.getHead()) * WEIGHT_ARRAY_STORE;
        total += sumCounterList(ArrayNew.getHead()) * WEIGHT_ARRAY_NEW;

        return total;
    }

    public static void printReport() {
        System.out.println("=== Cost Report ===");
        System.out.println("MethodCall: " + sumCounterList(MethodCall.getHead()));
        System.out.println("MethodEntry: " + sumCounterList(MethodEntry.getHead()));
        System.out.println("Jump: " + sumCounterList(Jump.getHead()));
        System.out.println("CompareInt: " + sumCounterList(CompareInt.getHead()));
        System.out.println("CompareObject: " + sumCounterList(CompareObject.getHead()));
        System.out.println("CompareLong: " + sumCounterList(CompareLong.getHead()));
        System.out.println("VariableLoad: " + sumCounterList(VariableLoad.getHead()));
        System.out.println("VariableStore: " + sumCounterList(VariableStore.getHead()));
        System.out.println("ArithmeticAddSubNeg: " + sumCounterList(ArithmeticAddSubNeg.getHead()));
        System.out.println("ArithmeticMul: " + sumCounterList(ArithmeticMul.getHead()));
        System.out.println("ArithmeticDivRem: " + sumCounterList(ArithmeticDivRem.getHead()));
        System.out.println("ArrayLoad: " + sumCounterList(ArrayLoad.getHead()));
        System.out.println("ArrayStore: " + sumCounterList(ArrayStore.getHead()));
        System.out.println("ArrayNew: " + sumCounterList(ArrayNew.getHead()));
        System.out.println("Total Cost: " + calculateTotalCost());
    }
}