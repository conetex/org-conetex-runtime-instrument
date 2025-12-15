package org.conetex.contract.runtime.instrument.interfaces;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.Set;

public interface RetransformingClassFileTransformer extends ClassFileTransformer {

    Set<String> getHandledClasses();

    Set<String> getTransformFailedClasses();

    Set<String> getTransformSkippedClasses();

    // used by agent for instrumentation
    void triggerRetransform(Instrumentation inst, Class<?>[] allClasses);

    void initMainClassJvmName(String mainClassJvmName);

    void resetCounters();

    // used by agent for reporting
    Counter[] getCounters();

    int[] getCounterWeights();


    long[] report();
}
