package org.conetex.runtime.instrument.test.jar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class MainJarTest {

    @BeforeEach
    void setUp() {
        System.out.println("org.conetex.runtime.instrument.test.jar MainTest working here: " + new File(".").getAbsolutePath());
        MainJar.warmup();
    }

    @Test
    void testsIncrementableCounterDefault() {
        // Arrange:
        String result;
        // Act
        result = MainJar.testsIncrementableCounterDefault();
        // Assert
        assertFalse( result.contains(MainJar.TEST_FAILED) );
    }

    @Test
    void testsIncrementableCounterBlock() {
        // Arrange:
        String result;
        // Act
        result = MainJar.testsIncrementableCounterBlock();
        // Assert
        assertFalse( result.contains(MainJar.TEST_FAILED) );
    }

    //@Test todo for whatever reason this does not work with modules
    void testsIncrementableInterfaceDefault() {
        // Arrange:
        String result;
        // Act
        result = MainJar.testsIncrementableInterfaceDefault();
        // Assert
        assertFalse( result.contains(MainJar.TEST_FAILED) );
    }

    @Test
    void testsIncrementableInterfaceBlock() {
        // Arrange:
        String result;
        // Act
        result = MainJar.testsIncrementableInterfaceBlock();
        // Assert
        assertFalse( result.contains(MainJar.TEST_FAILED) );
    }

    @Test
    void testsChainsOfLongsCountersWeighted() {
        // Arrange:
        String result;
        // Act
        result = MainJar.testsChainsOfLongsCountersWeighted();
        // Assert
        assertFalse( result.contains(MainJar.TEST_FAILED) );
    }

    //@Test todo for whatever reason this does not work with modules
    void testsChainsOfLongsInterface() {
        // Arrange:
        String result;
        // Act
        result = MainJar.testsChainsOfLongsInterface();
        // Assert
        assertFalse( result.contains(MainJar.TEST_FAILED) );
    }

    //@Test
    void testsCountersReset() {
        // Arrange:
        String result;
        // Act
        result = MainJar.testsCountersReset();
        // Assert
        assertFalse( result.contains(MainJar.TEST_FAILED) );
    }

    @Test
    void testsCountersBlockIncrement() {
        // Arrange:
        String result;
        // Act
        result = MainJar.testsCountersBlockIncrement();
        // Assert
        assertFalse( result.contains(MainJar.TEST_FAILED) );
    }

}