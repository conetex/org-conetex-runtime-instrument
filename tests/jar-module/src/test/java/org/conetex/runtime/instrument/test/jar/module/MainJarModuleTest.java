package org.conetex.runtime.instrument.test.jar.module;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MainJarModuleTest {

    @Test
    void testsIncrementableCounterDefault() {
        // Arrange:

        // Act & Assert
        assertDoesNotThrow( ()-> MainJarModule.main(null) );
    }

}