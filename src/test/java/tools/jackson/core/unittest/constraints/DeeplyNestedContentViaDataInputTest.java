package tools.jackson.core.unittest.constraints;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;

import org.junit.jupiter.api.Test;

import tools.jackson.core.*;
import tools.jackson.core.exc.StreamConstraintsException;
import tools.jackson.core.json.JsonFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Nesting Depth Constraint Bypass in UTF8DataInputJsonParser
 */
class DeeplyNestedContentViaDataInputTest
{
    private static final int TEST_NESTING_DEPTH = 5000;

    private final JsonFactory factory = new JsonFactory();

    // [core#1553] Regression; works in 2.x
    @Test
    void dataInputParserBypassesNestingDepth() throws Exception {
        byte[] data = buildNestedArrays(TEST_NESTING_DEPTH);
        DataInput di = new DataInputStream(new ByteArrayInputStream(data));
        int maxDepth = 0;
        try (JsonParser p = factory.createParser(ObjectReadContext.empty(), di)) {
            while (p.nextToken() != null) {
                int depth = p.streamReadContext().getNestingDepth();
                if (depth > maxDepth) {
                    maxDepth = depth;
                }
            }
            fail("Should not pass");
        } catch (StreamConstraintsException e) {
            // Expected
            String msg = e.getMessage();
            assertTrue(msg.contains("Document nesting depth"),
                    "Unexpected exception message: " + msg);
        }
        // Verify that we did hit deeply nested content
        assertTrue(maxDepth > 100, "Should have traversed deeper than 100, got: " + maxDepth);
    }

    private byte[] buildNestedArrays(int depth) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append('[');
        }
        sb.append('0');
        for (int i = 0; i < depth; i++) {
            sb.append(']');
        }
        return sb.toString().getBytes();
    }
}
