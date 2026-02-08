package org.conetex.runtime.instrument.test.integration;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.conetex.utilz.Property.loadProperties;
import static org.conetex.utilz.Property.resolveProperty;
import static org.junit.jupiter.api.Assertions.*;

class AgentIntegrationTest {

    static final int STATUS_BLOCKED = 403; // see org.conetex.runtime.instrument.metrics.cost.Transformer.STATUS_BLOCKED

    //@Test todo reactivate (it did not resonse)
    void testInstrumentationInSeparateJvm() throws Exception {
        // since we are working in <projectRoot>/test/integration and agent is located in <projectRoot>/agent the command is:
        // java -javaagent:../../agent/target/agent-0.0.1-SNAPSHOT-fat.jar=pathToTransformerJar:../../metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT-fat.jar -cp ../jar/target/jar-0.0.1-SNAPSHOT.jar org.conetex.runtime.instrument.test.jar.Main

        Process process = process("jar", "org.conetex.runtime.instrument.test.jar.Main").start();

        String stdOutput;
        try (InputStream is = process.getInputStream()) {
            stdOutput = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
        String errOutput;
        try (InputStream is = process.getErrorStream()) {
            errOutput = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        int exitCode = process.waitFor();

        System.out.println("=== test default JVM OUTPUT ==>");
        System.err.println(errOutput);
        System.out.println(stdOutput);
        System.out.println("<== test default JVM OUTPUT ===");

        // --- Assertions ---
        assertEquals(0, exitCode, "JVM exited with non-zero status");
        assertFalse(stdOutput.contains("test failed"), "stdOutput does not contain 'test failed'");
        assertFalse(errOutput.contains("test failed"), "errOutput does not contain 'test failed'");

    }

    @Test
    void testInstrumentationOfBlockedJarInSeparateJvm() throws Exception {
        Process process = process("jar-blocked","org.conetex.runtime.instrument.test.jar.blocked.Main")
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start();

        System.out.println("=== block test JVM OUTPUT ==>");
        int exitCode = process.waitFor();
        System.out.println("exit code " + exitCode);
        System.out.println("<== block test JVM OUTPUT ===");

        // --- Assertions ---
        assertEquals(STATUS_BLOCKED, exitCode, "JVM exited with " + STATUS_BLOCKED + " status");
    }

    private static ProcessBuilder process(String moduleToTest, String classToTest) throws IOException {

        String version = resolveProperty(
            loadProperties(
                    AgentIntegrationTest.class,
                    "version.properties"
            ),
            "version"
        );
        System.out.println("version: " + version);

        Path agentJar = Path.of("../../agent/target/agent-" + version + ".jar").toAbsolutePath();
        Path testJar = Path.of("../" + moduleToTest + "/target/" + moduleToTest + "-" + version + ".jar").toAbsolutePath();

        ProcessBuilder pb = new ProcessBuilder(
                "java",
                "-javaagent:" + agentJar + "=pathToTransformerJar:" +
                        "../../metrics-cost/target/metrics-cost-" + version + "-fat.jar" +
                        ",../../metrics-cost-unnamed/target/metrics-cost-unnamed-" + version + ".jar",
                "-cp",
                testJar.toString(),
                classToTest
        );

        return pb;
    }

}

