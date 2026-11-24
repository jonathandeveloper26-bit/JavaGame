package ripplepond.model.element.predator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ripplepond.model.geometry.Vec2;

import static org.junit.jupiter.api.Assertions.assertThrows;

class StraightLineChaseTest {

    private final StraightLineChase chase = new StraightLineChase();

    @Test
    void movesExactlyOneStepTowardsTheTarget() {
        Vec2 next = chase.nextPosition(new Vec2(0, 0), new Vec2(1, 0), 0.06);
        assertEquals(0.06, next.getX(), 1e-9);
        assertEquals(0.0, next.getY(), 1e-9);
    }

    @Test
    void stopsAtTheTargetWhenItIsCloserThanOneStep() {
        Vec2 next = chase.nextPosition(new Vec2(0, 0), new Vec2(0.03, 0.04), 0.06);
        assertEquals(0.03, next.getX(), 1e-9);
        assertEquals(0.04, next.getY(), 1e-9);
    }

    @Test
    void neverOvershoots() {
        Vec2 target = new Vec2(0.2, 0.2);
        Vec2 next = chase.nextPosition(new Vec2(0.2, 0.2), target, 0.5);
        assertEquals(0.2, next.getX(), 1e-9);
        assertEquals(0.2, next.getY(), 1e-9);
    }
    @Test
    void zeroStepStaysPut() {
        Vec2 next = chase.nextPosition(new Vec2(0.3, 0.3), new Vec2(1, 1), 0);
        assertEquals(0.3, next.getX(), 1e-9);
        assertEquals(0.3, next.getY(), 1e-9);
    }

    @Test
    void negativeStepIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> chase.nextPosition(new Vec2(0, 0), new Vec2(1, 0), -0.1));
    }
}