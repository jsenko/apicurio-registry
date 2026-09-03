package io.apicurio.registry.probe;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

/**
 * Reports how a literal %2F in a path segment is handled by the default stack. This is a
 * diagnostic spike, not an assertion test - it prints status + body for each route shape.
 */
@QuarkusTest
public class EncodedSlashProbeTest {

    private void probe(String label, String path) {
        Response r = RestAssured.given()
                .urlEncodingEnabled(false)
                .when()
                .get(path)
                .andReturn();
        System.out.println("PROBE " + label + " -> path=" + path
                + " status=" + r.getStatusCode()
                + " body=[" + r.getBody().asString().replace("\n", " ").trim() + "]");
    }

    @Test
    void probeEncodedSlash() {
        System.out.println("==== ENCODED-SLASH PROBE (default config) ====");
        // Controls: plain unencoded forms must work.
        probe("single-plain", "/probe/one/weather");
        probe("two-plain", "/probe/two/io.github.test/weather");

        // The real question: a single segment carrying a literal %2F.
        probe("single-%2F", "/probe/one/io.github.test%2Fweather");
        probe("single-enc-%2F", "/probe/one-enc/io.github.test%2Fweather");
        probe("single-regex-%2F", "/probe/one-regex/io.github.test%2Fweather");

        // And what the real MCP two-segment route would see for a %2F name.
        probe("two-%2F", "/probe/two/io.github.test%2Fweather/x");

        // Traversal / multi-slash: does the container hand dangerous strings to the handler?
        // (Safety must then come from McpServerName validation, not the router.)
        probe("traversal", "/probe/one/a%2F..%2F..%2Fetc");
        probe("double-slash", "/probe/one/a%2Fb%2Fc");
        probe("encoded-dot", "/probe/one-enc/..%2F..%2Fx");
        System.out.println("==== END PROBE ====");
    }
}
