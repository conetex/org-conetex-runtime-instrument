package org.conetex.runtime.test.instrument.count.opcodes;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class MainCountOpcodes2Test {
    @Test
    void testsB() {
        // Arrange
        System.out.println("MainCountOpcodesTest B working here: " + new File(".").getAbsolutePath());
        // Act and Assert
        assertDoesNotThrow(
                ()->{
                    MainCountOpcodes.mainB(null);
                }
        );
    }
}