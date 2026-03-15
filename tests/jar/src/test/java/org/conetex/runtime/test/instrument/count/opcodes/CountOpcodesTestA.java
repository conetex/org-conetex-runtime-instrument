package org.conetex.runtime.test.instrument.count.opcodes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CountOpcodesTestA {

    @BeforeEach
    void setUp() {
        System.out.println("org.conetex.runtime.instrument.test.jar MainTest working here: " + new File(".").getAbsolutePath());
    }

    @Test
    void testsA() {
        // Arrange
        System.out.println("MainCountOpcodesTest A working here: " + new File(".").getAbsolutePath());
        // Act and Assert
        assertDoesNotThrow(
                ()->{
                    CountOpcodesA.mainA(null);
                }
        );
    }

    @Test
    void testsB() {
        // Arrange
        System.out.println("MainCountOpcodesTest B working here: " + new File(".").getAbsolutePath());
        // Act and Assert
        assertDoesNotThrow(
                ()->{
                    CountOpcodesA.mainB(null);
                }
        );
    }

}