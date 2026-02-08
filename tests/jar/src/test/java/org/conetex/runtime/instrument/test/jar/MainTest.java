package org.conetex.runtime.instrument.test.jar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @BeforeEach
    void setUp() {
        System.out.println("org.conetex.runtime.instrument.test.jar MainTest working here: " + new File(".").getAbsolutePath());
        Main.warmup();
    }

    @Test
    void testsIncrementableCounterDefault() {
        // Arrange:
        String result;
        // Act
        result = Main.testsIncrementableCounterDefault();
        // Assert
        assertFalse( result.contains(Main.TEST_FAILED) );
    }

    @Test
    void testsIncrementableCounterBlock() {
        // Arrange:
        String result;
        // Act
        result = Main.testsIncrementableCounterBlock();
        // Assert
        assertFalse( result.contains(Main.TEST_FAILED) );
    }

    //@Test todo for whatever reason this does not work with modules
    void testsIncrementableInterfaceDefault() {
        // Arrange:
        String result;
        // Act
        result = Main.testsIncrementableInterfaceDefault();
        // Assert
        assertFalse( result.contains(Main.TEST_FAILED) );
    }

    @Test
    void testsIncrementableInterfaceBlock() {
        // Arrange:
        String result;
        // Act
        result = Main.testsIncrementableInterfaceBlock();
        // Assert
        assertFalse( result.contains(Main.TEST_FAILED) );
    }

    @Test
    void testsChainsOfLongsCountersWeighted() {
        // Arrange:
        String result;
        // Act
        result = Main.testsChainsOfLongsCountersWeighted();
        // Assert
        assertFalse( result.contains(Main.TEST_FAILED) );
    }

    //@Test todo for whatever reason this does not work with modules
    void testsChainsOfLongsInterface() {
        // Arrange:
        String result;
        // Act
        result = Main.testsChainsOfLongsInterface();
        // Assert
        assertFalse( result.contains(Main.TEST_FAILED) );
    }

    @Test
    void testsCountersReset() {
        // Arrange:
        String result;
        // Act
        result = Main.testsCountersReset();
        // Assert
        assertFalse( result.contains(Main.TEST_FAILED) );
    }

    @Test
    void testsCountersBlockIncrement() {
        // Arrange:
        String result;
        // Act
        result = Main.testsCountersBlockIncrement();
        // Assert
        assertFalse( result.contains(Main.TEST_FAILED) );
    }

}