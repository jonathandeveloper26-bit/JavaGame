package ripplepond.model.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ripplepond.model.geometry.Vec2;

class AreaTest {

    @Test
    void includesTheTopEdgeAndExcludesTheBottomEdge() {
        Area water = new Area(AreaType.WATER, 0.1, 0.5);
        assertTrue(water.contains(new Vec2(0.5, 0.1)));
        assertFalse(water.contains(new Vec2(0.5, 0.5)));
    }

    @Test
    void containsPointsInsideAndRejectsPointsOutside() {
        Area water = new Area(AreaType.WATER, 0.1, 0.5);
        assertTrue(water.contains(new Vec2(0.5, 0.3)));
        assertFalse(water.contains(new Vec2(0.5, 0.05)));
        assertFalse(water.contains(new Vec2(0.5, 0.6)));
    }

    @Test
    void reportsItsType() {
        assertEquals(AreaType.FAR_BANK, new Area(AreaType.FAR_BANK, 0, 0.1).getType());
    }

    @Test
    void rejectsAStripWithBottomNotBelowTop() {
        assertThrows(IllegalArgumentException.class, () -> new Area(AreaType.WATER, 0.5, 0.5));
        assertThrows(IllegalArgumentException.class, () -> new Area(AreaType.WATER, 0.5, 0.2));
    }

    @Test
    void rejectsNullType() {
        assertThrows(NullPointerException.class, () -> new Area(null, 0, 0.1));
    }
}
