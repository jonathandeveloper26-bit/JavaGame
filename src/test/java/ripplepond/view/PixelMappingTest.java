package ripplepond.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ripplepond.model.geometry.Vec2;

class PixelMappingTest {

    private static final double DELTA = 1e-9;

    @Test
    void theAreaFillsTheWindowWhenTheShapeMatches() {
        PixelMapping map = new PixelMapping(900, 600); // 3:2 exactly

        assertEquals(900, map.scale(), DELTA);
        PixelMapping.Point origin = map.toPixels(new Vec2(0, 0));
        assertEquals(0, origin.x(), DELTA);
        assertEquals(0, origin.y(), DELTA);
    }

    @Test
    void awiderWindowIsCentredWithSideMargins() {
        PixelMapping map = new PixelMapping(1000, 600);

        assertEquals(900, map.scale(), DELTA);
        assertEquals(50, map.toPixels(new Vec2(0, 0)).x(), DELTA);
        assertEquals(0, map.toPixels(new Vec2(0, 0)).y(), DELTA);
    }

    @Test
    void aTallerWindowIsCentredWithTopAndBottomMargins() {
        PixelMapping map = new PixelMapping(900, 800);

        assertEquals(900, map.scale(), DELTA);
        assertEquals(0, map.toPixels(new Vec2(0, 0)).x(), DELTA);
        assertEquals(100, map.toPixels(new Vec2(0, 0)).y(), DELTA);
    }

    @Test
    void bothAxesUseTheSameScale() {
        PixelMapping map = new PixelMapping(1000, 700);
        PixelMapping.Point origin = map.toPixels(new Vec2(0, 0));
        PixelMapping.Point across = map.toPixels(new Vec2(0.1, 0));
        PixelMapping.Point down = map.toPixels(new Vec2(0, 0.1));

        assertEquals(0.1 * map.scale(), across.x() - origin.x(), DELTA);
        assertEquals(0.1 * map.scale(), down.y() - origin.y(), DELTA);
    }

    @Test
    void aPointSurvivesTheRoundTrip() {
        PixelMapping map = new PixelMapping(1000, 700);
        Vec2 point = new Vec2(0.37, 0.25);
        PixelMapping.Point pixels = map.toPixels(point);
        Vec2 back = map.toFraction(pixels.x(), pixels.y());

        assertEquals(point.getX(), back.getX(), DELTA);
        assertEquals(point.getY(), back.getY(), DELTA);
    }

    @Test
    void marginsAreOutsideThePlayArea() {
        PixelMapping map = new PixelMapping(1000, 600);

        assertFalse(map.inPlayArea(10, 300), "left margin");
        assertTrue(map.inPlayArea(500, 300), "middle");
        assertFalse(map.inPlayArea(990, 300), "right margin");
    }

    @Test
    void rejectsAnEmptyWindow() {
        assertThrows(IllegalArgumentException.class, () -> new PixelMapping(0, 600));
        assertThrows(IllegalArgumentException.class, () -> new PixelMapping(900, -1));
    }
}
