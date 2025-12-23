package org.conetex.runtime.instrument.metrics.cost;

import org.conetex.runtime.instrument.counter.CountersWeighted;
import org.conetex.runtime.instrument.counter.LongLimits;
import org.conetex.runtime.instrument.counter.Counter;
import org.conetex.runtime.instrument.interfaces.ChainOfLongs;

public final class CostCounters {

    public final static int[] WEIGHTS = new int[] {
            35,//165, // ArithmeticAddSubNeg
            109,//890, // ArithmeticDivRem
            43,//956, // ArithmeticMul
            48,//352, // ArrayLoad
            98,//901, // ArrayNew
            65,//934, // ArrayStore
            30,//769, // CompareInt
            39,//560, // CompareLong
            43,//956, // CompareObject
            109,//890, // ExceptionThrow
            52,//747, // FieldLoad
            61,//539, // FieldStore
            28,//571, // Jump
            54,//945, // MethodCall
            0,//  0, // MethodEntry
            76,//923, // Monitor
            50,//550, // VariableLoad
            21,//978, // VariableStore
            26,//374  // TypeCheck
    };

    public final static LongLimits CONFIG_MIN_MAX = new LongLimits(0L, 10L);

    public static final Counter ARITHMETIC_ADD_SUB_NEG = new Counter(CONFIG_MIN_MAX);
    public static final Counter ARITHMETIC_DIV_REM = new Counter(CONFIG_MIN_MAX);
    public static final Counter ARITHMETIC_MUL = new Counter(CONFIG_MIN_MAX);
    public static final Counter ARRAY_LOAD = new Counter(CONFIG_MIN_MAX);
    public static final Counter ARRAY_NEW = new Counter(CONFIG_MIN_MAX);
    public static final Counter ARRAY_STORE = new Counter(CONFIG_MIN_MAX);
    public static final Counter COMPARE_INT = new Counter(CONFIG_MIN_MAX);
    public static final Counter COMPARE_LONG = new Counter(CONFIG_MIN_MAX);
    public static final Counter COMPARE_OBJECT = new Counter(CONFIG_MIN_MAX);
    public static final Counter EXCEPTION_THROW = new Counter(CONFIG_MIN_MAX);
    public static final Counter FIELD_LOAD = new Counter(CONFIG_MIN_MAX);
    public static final Counter FIELD_STORE = new Counter(CONFIG_MIN_MAX);
    public static final Counter JUMP = new Counter(CONFIG_MIN_MAX);
    public static final Counter METHOD_CALL = new Counter(CONFIG_MIN_MAX);
    public static final Counter METHOD_ENTRY = new Counter(CONFIG_MIN_MAX);
    public static final Counter MONITOR = new Counter(CONFIG_MIN_MAX);
    public static final Counter VARIABLE_LOAD = new Counter(CONFIG_MIN_MAX);
    public static final Counter VARIABLE_STORE = new Counter(CONFIG_MIN_MAX);
    public static final Counter TYPE_CHECK = new Counter(CONFIG_MIN_MAX);

    public final static CountersWeighted CONFIG = new CountersWeighted(CONFIG_MIN_MAX,
            new ChainOfLongs[]{
                    CostCounters.ARITHMETIC_ADD_SUB_NEG,
                    CostCounters.ARITHMETIC_DIV_REM,
                    CostCounters.ARITHMETIC_MUL,
                    CostCounters.ARRAY_LOAD,
                    CostCounters.ARRAY_NEW,
                    CostCounters.ARRAY_STORE,
                    CostCounters.COMPARE_INT,
                    CostCounters.COMPARE_LONG,
                    CostCounters.COMPARE_OBJECT,
                    CostCounters.EXCEPTION_THROW,
                    CostCounters.FIELD_LOAD,
                    CostCounters.FIELD_STORE,
                    CostCounters.JUMP,
                    CostCounters.METHOD_CALL,
                    CostCounters.METHOD_ENTRY,
                    CostCounters.MONITOR,
                    CostCounters.VARIABLE_LOAD,
                    CostCounters.VARIABLE_STORE,
                    CostCounters.TYPE_CHECK
            }
            ,
            WEIGHTS);


    @SuppressWarnings("unused")
    public static void incrementArithmeticAddSubNeg() {
        CostCounters.ARITHMETIC_ADD_SUB_NEG.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementArithmeticDivRem() {
        CostCounters.ARITHMETIC_DIV_REM.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementArithmeticMul() {
        CostCounters.ARITHMETIC_MUL.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementArrayLoad() {
        CostCounters.ARRAY_LOAD.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementArrayNew() {
        CostCounters.ARRAY_NEW.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementArrayStore() {
        CostCounters.ARRAY_STORE.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementCompareInt() {
        CostCounters.COMPARE_INT.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementCompareLong() {
        CostCounters.COMPARE_LONG.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementCompareObject() {
        CostCounters.COMPARE_OBJECT.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementExceptionThrow() {
        CostCounters.EXCEPTION_THROW.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementFieldLoad() {
        CostCounters.FIELD_LOAD.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementFieldStore() {
        CostCounters.FIELD_STORE.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementJump() {
        CostCounters.JUMP.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementMethodCall() {
        CostCounters.METHOD_CALL.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementMethodEntry() {
        CostCounters.METHOD_ENTRY.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementMonitor() {
        CostCounters.MONITOR.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementVariableLoad() {
        CostCounters.VARIABLE_LOAD.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementVariableStore() {
        CostCounters.VARIABLE_STORE.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementTypeCheck() {
        CostCounters.TYPE_CHECK.increment();
    }

}
