package ripplepond.model.element;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ThrowableKind}: the sizes match the level format and grow from twig
 * to log.
 */
class ThrowableKindTest {

    private static final double DELTA = 1e-12;

    @Test
    void thereAreThreeKindsFromSmallestToLargest() {
        assertArrayEquals(
                new ThrowableKind[] {ThrowableKind.TWIG, ThrowableKind.PLANK, ThrowableKind.LOG},
                ThrowableKind.values());
    }

    @Test
    void sizesMatchTheLevelFormat() {
        assertEquals(0.05, ThrowableKind.TWIG.size(), DELTA);
        assertEquals(0.10, ThrowableKind.PLANK.size(), DELTA);
        assertEquals(0.18, ThrowableKind.LOG.size(), DELTA);
    }

    @Test
    void eachKindIsBiggerThanTheOneBefore() {
        ThrowableKind[] kinds = ThrowableKind.values();

        for (int i = 1; i < kinds.length; i++) {
            assertTrue(kinds[i].size() > kinds[i - 1].size(), kinds[i].name());
        }
    }
}
