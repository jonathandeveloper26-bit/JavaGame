package ripplepond.model.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link LevelFormatException}: the message starts with the line number when
 * there is one.
 */
class LevelFormatExceptionTest {

    @Test
    void aLineProblemStartsWithTheLineNumber() {
        LevelFormatException error = new LevelFormatException(7, "unknown keyword 'frgo'");

        assertEquals("line 7: unknown keyword 'frgo'", error.getMessage());
        assertEquals(7, error.getLineNumber());
    }

    @Test
    void aFileProblemHasNoLineNumber() {
        LevelFormatException error = new LevelFormatException("the frog line is missing");

        assertEquals("the frog line is missing", error.getMessage());
        assertEquals(0, error.getLineNumber());
    }
}
