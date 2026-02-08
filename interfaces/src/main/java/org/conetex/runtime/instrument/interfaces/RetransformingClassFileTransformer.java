package org.conetex.runtime.instrument.interfaces;

import org.conetex.runtime.instrument.interfaces.arithmetic.ChainsOfLongs;
import org.conetex.runtime.instrument.interfaces.arithmetic.ResultLongDividedByInt;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.Set;

public interface RetransformingClassFileTransformer extends ClassFileTransformer {

    @SuppressWarnings("unused")
    Set<String> getHandledClasses();

    @SuppressWarnings("unused")
    Set<String> getTransformFailedClasses();

    @SuppressWarnings("unused")
    Set<String> getTransformSkippedClasses();

    // ----------------------------------------------------
    // used by agent for instrumentation

    @SuppressWarnings("unused")
    void triggerRetransform(Instrumentation inst, Class<?>[] allClasses);

    @SuppressWarnings("unused")
    void initMainClassJvmName(String mainClassJvmName);

    @SuppressWarnings("unused")
    void addToHandledClasses(String handledClassJvmName);

    @SuppressWarnings("unused")
    void resetCounters();

    // ----------------------------------------------------
    // used by agent for managing

    @SuppressWarnings("unused")
    void blockIncrement(boolean incrementationBlocked);

    // ----------------------------------------------------
    // used by agent for reporting

    ChainsOfLongs getConfig();

    @SuppressWarnings("unused")
    ResultLongDividedByInt[] report();

}
