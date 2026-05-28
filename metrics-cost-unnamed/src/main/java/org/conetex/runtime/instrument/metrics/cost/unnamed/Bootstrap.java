package org.conetex.runtime.instrument.metrics.cost.unnamed;

import java.lang.invoke.*;

// todo has this class to be here in unnamed module?
public class Bootstrap {

    public static class BootstrapCyclicCallException extends IllegalStateException {
        public BootstrapCyclicCallException(String message) {
            super(message);
        }
    }

    // BOOTSTRAP CallSite
    static boolean bootstrapInProgress = false;

    public static synchronized CallSite callSite(MethodHandles.Lookup lookup, String name, MethodType type) throws NoSuchMethodException, IllegalAccessException {
        return callSite(lookup, name, type, CounterMethods.class);
    }

    public static synchronized CallSite callSite(MethodHandles.Lookup lookup, String name, MethodType type,
                                                 Class<?> realOwner) throws NoSuchMethodException, IllegalAccessException {

        if (bootstrapInProgress) {
            //return new ConstantCallSite(MethodHandles.empty(type));
            throw new BootstrapCyclicCallException("bootstrapInProgress");
        }
        bootstrapInProgress = true;
        try {
            Module realOwnerModule = realOwner.getModule();
            if (!realOwnerModule.isExported(realOwner.getPackageName())) {
                throw new NoSuchMethodException(realOwnerModule + " does not export " + realOwner.getPackageName());
            }

            //System.out.println("bootstrap: " + realOwner + " | " + name + " | " + type);
            // Link the dynamic method to an actual implementation elsewhere (possibly in another class)
            MethodHandle targetMethodHandle = null;
            try {
                targetMethodHandle = lookup.findStatic(
                        realOwner, // Real owner of the "incrementCompareInt" method
                        name,      // Actual method name
                        type       // Method descriptor
                );
            } catch (IllegalAccessException e) {
                //System.err.println("bootstrap IllegalAccessException: " + realOwner + " | " + name + " | " + type + " | " + e.getMessage() + " ");
                throw e;
            } catch (Throwable e) {
                //System.err.println("bootstrap Throwable: " + realOwner + " | " + name + " | " + type + " | " + e.getMessage() + " ");
                throw e;
            }

            // Return a CallSite that links to the targetMethodHandle
            return new ConstantCallSite(targetMethodHandle);
        }
        finally {
            bootstrapInProgress = false;
        }
    }
}
