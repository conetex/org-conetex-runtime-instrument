package org.conetex.runtime.instrument.test.jar;

import org.conetex.runtime.instrument.metrics.cost.Counters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class MainJarTest {

    @BeforeEach
    void setUp() {
        System.out.println("org.conetex.runtime.instrument.test.jar MainTest working here: " + new File(".").getAbsolutePath());
        //MainJar.warmup();
    }

    @Test //todo for whatever reason this does not work with modules
    void testsIncrementableAInterfaceDefault() {
        // Arrange
        // Act & Assert
        assertDoesNotThrow( () -> MainJar.main(null) );
    }

}