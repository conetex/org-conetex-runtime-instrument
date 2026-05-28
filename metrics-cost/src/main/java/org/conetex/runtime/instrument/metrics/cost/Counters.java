package org.conetex.runtime.instrument.metrics.cost;

import org.conetex.runtime.instrument.counter.CountersWeighted;
import org.conetex.runtime.instrument.counter.LongLimits;
import org.conetex.runtime.instrument.counter.Counter;

public final class Counters {

    static {
        System.err.println("Counters loaded: " + Counters.class + " (class) - " + Counters.class.getModule() + " (module) - " + Counter.class.getClassLoader() + " (loader)");
    }

    public final static LongLimits CONFIG_MIN_MAX = new LongLimits(0L, Long.MAX_VALUE);

    public static final Counter ARITHMETIC_ADD_SUB_NEG = new Counter(CONFIG_MIN_MAX, true);
    public static final Counter ARITHMETIC_DIV_REM = new Counter(CONFIG_MIN_MAX, true);
    public static final Counter ARITHMETIC_MUL = new Counter(CONFIG_MIN_MAX, true);
    public static final Counter ARRAY_LOAD = new Counter(CONFIG_MIN_MAX, true);
    public static final Counter ARRAY_NEW = new Counter(CONFIG_MIN_MAX, true);
    public static final Counter ARRAY_STORE = new Counter(CONFIG_MIN_MAX, true);
    public static final Counter COMPARE_INT = new Counter(CONFIG_MIN_MAX, true);
    public static final Counter COMPARE_LONG = new Counter(CONFIG_MIN_MAX, true);
    public static final Counter COMPARE_OBJECT = new Counter(CONFIG_MIN_MAX, true);
    public static final Counter EXCEPTION_THROW = new Counter(CONFIG_MIN_MAX, true);
    public static final Counter FIELD_LOAD = new Counter(CONFIG_MIN_MAX, true);
    public static final Counter FIELD_STORE = new Counter(CONFIG_MIN_MAX, true);
    public static final Counter JUMP = new Counter(CONFIG_MIN_MAX, true);
    public static final Counter METHOD_CALL = new Counter(CONFIG_MIN_MAX, true);
    public static final Counter METHOD_ENTRY = new Counter(CONFIG_MIN_MAX, true);
    public static final Counter MONITOR = new Counter(CONFIG_MIN_MAX, true);
    public static final Counter VARIABLE_LOAD = new Counter(CONFIG_MIN_MAX, true);
    public static final Counter VARIABLE_STORE = new Counter(CONFIG_MIN_MAX, true);
    public static final Counter TYPE_CHECK = new Counter(CONFIG_MIN_MAX, true);

    public static final Counter[] COUNTERS = new Counter[]{
                Counters.ARITHMETIC_ADD_SUB_NEG,
                Counters.ARITHMETIC_DIV_REM,
                Counters.ARITHMETIC_MUL,
                Counters.ARRAY_LOAD,
                Counters.ARRAY_NEW,
                Counters.ARRAY_STORE,
                Counters.COMPARE_INT,
                Counters.COMPARE_LONG,
                Counters.COMPARE_OBJECT,
                Counters.EXCEPTION_THROW,
                Counters.FIELD_LOAD,
                Counters.FIELD_STORE,
                Counters.JUMP,
                Counters.METHOD_CALL,
                Counters.METHOD_ENTRY,
                Counters.MONITOR,
                Counters.VARIABLE_LOAD,
                Counters.VARIABLE_STORE,
                Counters.TYPE_CHECK
    };

    public final static CountersWeighted CONFIG = new CountersWeighted(
            CONFIG_MIN_MAX,
            COUNTERS,
            new int[] {
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
            }
            );

    public static void reset() {
        for(Counter c : COUNTERS){
            c.reset();
        }
    }

    public static void blockIncrement(boolean incrementationBlocked) {
        for(Counter c : COUNTERS){
            c.blockIncrement(incrementationBlocked);
        }
    }


    @SuppressWarnings("unused")
    public static void incrementArithmeticAddSubNeg() {
        Counters.ARITHMETIC_ADD_SUB_NEG.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementArithmeticDivRem() {
        Counters.ARITHMETIC_DIV_REM.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementArithmeticMul() {
        Counters.ARITHMETIC_MUL.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementArrayLoad() {
        Counters.ARRAY_LOAD.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementArrayNew() {
        Counters.ARRAY_NEW.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementArrayStore() {
        Counters.ARRAY_STORE.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementCompareInt() {
        Counters.COMPARE_INT.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementCompareLong() {
        Counters.COMPARE_LONG.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementCompareObject() {
        Counters.COMPARE_OBJECT.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementExceptionThrow() {
        Counters.EXCEPTION_THROW.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementFieldLoad() {
        Counters.FIELD_LOAD.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementFieldStore() {
        Counters.FIELD_STORE.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementJump() {
        Counters.JUMP.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementMethodCall() {
        Counters.METHOD_CALL.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementMethodEntry() {
        Counters.METHOD_ENTRY.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementMonitor() {
        Counters.MONITOR.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementVariableLoad() {
        Counters.VARIABLE_LOAD.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementVariableStore() {
        Counters.VARIABLE_STORE.increment();
    }

    @SuppressWarnings("unused")
    public static void incrementTypeCheck() {
        Counters.TYPE_CHECK.increment();
    }

}