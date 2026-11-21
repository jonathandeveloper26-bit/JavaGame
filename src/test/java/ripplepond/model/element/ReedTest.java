package ripplepond.model.element;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ripplepond.model.geometry.Vec2;

/**
 * Tests for {@link Reed}. Every test uses a clump centred at (0.5, 0.5) with radius
 * 0.25 (diameter 0.5); the numbers are exact in binary so the grazing case lands
 * exactly on the edge.
 */
class ReedTest {

    private final Reed reed = new Reed(new Vec2(0.5, 0.5), 0.5);

    @Test
    void segmentThroughTheCentreIsBlocked() {
        assertTrue(reed.liesBetween(new Vec2(0, 0.5), new Vec2(1, 0.5)));
    }

    @Test
    void segmentOrderDoesNotMatter() {
        assertTrue(reed.liesBetween(new Vec2(1, 0.5), new Vec2(0, 0.5)));
    }

    @Test
    void diagonalSegmentThroughTheClumpIsBlocked() {
        assertTrue(reed.liesBetween(new Vec2(0, 0), new Vec2(1, 1)));
    }

    @Test
    void segmentPassingClearlyOutsideIsNotBlocked() {
        assertFalse(reed.liesBetween(new Vec2(0, 0.125), new Vec2(1, 0.125)));
    }

    @Test
    void segmentGrazingTheEdgeIsNotBlocked() {
        // y = 0.25 is exactly one radius above the centre.
        assertFalse(reed.liesBetween(new Vec2(0, 0.25), new Vec2(1, 0.25)));
    }

    @Test
    void segmentThatStopsBeforeTheClumpIsNotBlocked() {
        // The line would hit the clump, but the segment ends at x = 0.125.
        assertFalse(reed.liesBetween(new Vec2(0, 0.5), new Vec2(0.125, 0.5)));
    }

    @Test
    void segmentThatStartsAfterTheClumpIsNotBlocked() {
        assertFalse(reed.liesBetween(new Vec2(0.875, 0.5), new Vec2(1, 0.5)));
    }

    @Test
    void segmentEndingInsideTheClumpIsBlocked() {
        assertTrue(reed.liesBetween(new Vec2(0, 0.5), new Vec2(0.5, 0.5)));
    }

    @Test
    void zeroLengthSegmentInsideTheClumpIsBlocked() {
        assertTrue(reed.liesBetween(new Vec2(0.5, 0.5), new Vec2(0.5, 0.5)));
    }

    @Test
    void zeroLengthSegmentOutsideTheClumpIsNotBlocked() {
        assertFalse(reed.liesBetween(new Vec2(0, 0), new Vec2(0, 0)));
    }

    @Test
    void rejectsNullEnds() {
        assertThrows(NullPointerException.class, () -> reed.liesBetween(null, new Vec2(0, 0)));
        assertThrows(NullPointerException.class, () -> reed.liesBetween(new Vec2(0, 0), null));
    }

    @Test
    void aReedIsAnElementOfTheGivenSize() {
        // Centres 0.5 apart, radii 0.25 + 0.25: they touch.
        assertTrue(reed.touches(new Reed(new Vec2(1, 0.5), 0.5)));
        // Centres 0.75 apart, radii 0.25 + 0.125: they do not.
        assertFalse(reed.touches(new Reed(new Vec2(1.25, 0.5), 0.25)));
    }
}
