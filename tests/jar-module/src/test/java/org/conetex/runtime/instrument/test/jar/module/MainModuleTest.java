package org.conetex.runtime.instrument.test.jar.module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class MainModuleTest {

    @Test
    void testsIncrementableCounterDefault() {
        // Arrange:

        // Act & Assert
        assertDoesNotThrow( ()-> Main.main(null) );
    }

}