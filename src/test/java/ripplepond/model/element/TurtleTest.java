package ripplepond.model.element;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import ripplepond.model.geometry.Vec2;

/**
 * Tests for {@link Turtle}. Waking and moving are tested in J8.
 */
class TurtleTest {

    private static final double DELTA = 1e-12;

    @Test
    void startsAsleep() {
        assertFalse(new Turtle(new Vec2(0.5, 0.5), 0.125).isAwake());
    }

    @Test
    void hasTheSizeAndPositionItIsGiven() {
        Turtle turtle = new Turtle(new Vec2(0.25, 0.5), 0.125);

        assertEquals(new Vec2(0.25, 0.5), turtle.getPosition());
        assertEquals(0.125, turtle.getSize(), DELTA);
    }

    @Test
    void rejectsBadArguments() {
        assertThrows(NullPointerException.class, () -> new Turtle(null, 0.125));
        assertThrows(IllegalArgumentException.class, () -> new Turtle(new Vec2(0.5, 0.5), 0));
    }
}
