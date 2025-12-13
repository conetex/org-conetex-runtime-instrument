package org.conetex.contract.runtime.instrument;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.Set;

public interface RetransformingClassFileTransformer extends ClassFileTransformer {

    Set<String> getHandledClasses();

    Set<String> getTransformFailedClasses();

    Set<String> getTransformSkippedClasses();

    void triggerRetransform(Instrumentation inst, Class<?>[] allClasses);

    void initMainClassJvmName(String mainClassJvmName);

    void resetCounters();

}
