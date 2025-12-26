package org.conetex.runtime.instrument.test.integration;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AgentIntegrationTest {

    @Test
    void testInstrumentationInSeparateJvm() throws Exception {
        // since we are working in <projectRoot>/test/integration and agent is located in <projectRoot>/agent the command is:
        // java -javaagent:../../agent/target/agent-0.0.1-SNAPSHOT-fat.jar=pathToTransformerJar:../../metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT-fat.jar -cp ../jar/target/jar-0.0.1-SNAPSHOT.jar org.conetex.runtime.instrument.test.jar.Main

        Process process = process();

        String stdOutput;
        try (InputStream is = process.getInputStream()) {
            stdOutput = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        String errOutput;
        try (InputStream is = process.getErrorStream()) {
            errOutput = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        int exitCode = process.waitFor();

        System.out.println("=== JVM OUTPUT ==>");
        System.err.println(errOutput);
        System.out.println(stdOutput);
        System.out.println("<== JVM OUTPUT ===");

        // --- Assertions ---
        assertEquals(0, exitCode, "JVM exited with non-zero status");
        assertFalse(stdOutput.contains("test failed"), "stdOutput does not contain 'test failed'");
        assertFalse(errOutput.contains("test failed"), "errOutput does not contain 'test failed'");

    }

    private static Process process() throws IOException {
        Path agentJar = Path.of("../../agent/target/agent-0.0.1-SNAPSHOT-fat.jar").toAbsolutePath();
        Path exampleJar = Path.of("../jar/target/jar-0.0.1-SNAPSHOT.jar").toAbsolutePath();

        ProcessBuilder pb = new ProcessBuilder(
                "java",
                "-javaagent:" + agentJar + "=pathToTransformerJar:" + "../../metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT-fat.jar",
                "-cp", exampleJar.toString(),
                "org.conetex.runtime.instrument.test.jar.Main"
        );

        return pb.start();
    }

}

