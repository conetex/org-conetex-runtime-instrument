package org.conetex.runtime.instrument.example.subpackage;

/**
 * Example class located in a different package than the main examples.
 * <p>
 * It is used to demonstrate access to and instrumentation of classes
 * across package boundaries.
 */
public class ClassFromOtherPackage {

    private ClassFromOtherPackage(){}

    /**
     * Prints a simple message to the console.
     * <p>
     * This method serves as a minimal example method that can be invoked
     * during instrumentation or integration tests.
     */
    public static void test(){
        System.out.println("ClassFromOtherPackage.test");
    }

}
