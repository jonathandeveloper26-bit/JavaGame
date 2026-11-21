package ripplepond.model.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Vec2}.
 */
class Vec2Test {

    private static final double EPS = 1e-9;

    /** Adding and subtracting give the expected coordinates and leave the inputs unchanged. */
    @Test
    void plusAndMinusGiveExpectedCoordinates() {
        Vec2 a = new Vec2(1, 2);
        Vec2 b = new Vec2(3, -4);

        Vec2 sum = a.plus(b);
        Vec2 difference = a.minus(b);

        assertEquals(4, sum.getX(), EPS);
        assertEquals(-2, sum.getY(), EPS);
        assertEquals(-2, difference.getX(), EPS);
        assertEquals(6, difference.getY(), EPS);
        assertEquals(1, a.getX(), EPS);
        assertEquals(2, a.getY(), EPS);
    }

    /** Scaling multiplies both components and returns a new vector. */
    @Test
    void scaleMultipliesBothComponents() {
        Vec2 v = new Vec2(2, -3);

        Vec2 scaled = v.scale(2.5);

        assertEquals(5, scaled.getX(), EPS);
        assertEquals(-7.5, scaled.getY(), EPS);
        assertEquals(2, v.getX(), EPS);
    }

    /** The length of (3, 4) is 5. */
    @Test
    void lengthOfThreeFourIsFive() {
        assertEquals(5, new Vec2(3, 4).length(), EPS);
    }

    /** A normalised vector has length 1 and keeps its direction. */
    @Test
    void normalisedHasLengthOneAndSameDirection() {
        Vec2 unit = new Vec2(3, 4).normalised();

        assertEquals(1, unit.length(), EPS);
        assertEquals(0.6, unit.getX(), EPS);
        assertEquals(0.8, unit.getY(), EPS);
    }

    /** The zero vector normalises to itself rather than producing NaN. */
    @Test
    void normalisedOfZeroReturnsZero() {
        Vec2 zero = new Vec2(0, 0);

        Vec2 result = zero.normalised();

        assertEquals(0, result.getX(), EPS);
        assertEquals(0, result.getY(), EPS);
    }

    /** Distance is the same in both directions. */
    @Test
    void distanceIsSymmetric() {
        Vec2 a = new Vec2(1, 1);
        Vec2 b = new Vec2(4, 5);

        assertEquals(5, a.distanceTo(b), EPS);
        assertEquals(5, b.distanceTo(a), EPS);
    }

    /** A step towards a far target moves by exactly the step size. */
    @Test
    void stepTowardsMovesByMaxStepWhenTargetIsFar() {
        Vec2 start = new Vec2(0, 0);
        Vec2 target = new Vec2(10, 0);

        Vec2 next = start.stepTowards(target, 0.5);

        assertEquals(0.5, next.getX(), EPS);
        assertEquals(0, next.getY(), EPS);
    }

    /** A step towards a near target lands exactly on the target. */
    @Test
    void stepTowardsReturnsTargetWhenCloserThanMaxStep() {
        Vec2 start = new Vec2(0, 0);
        Vec2 target = new Vec2(0.2, 0.1);

        Vec2 next = start.stepTowards(target, 1.0);

        assertEquals(target, next);
    }

    /** A zero step leaves the position where it is. */
    @Test
    void stepTowardsWithZeroStepStaysPut() {
        Vec2 start = new Vec2(1, 1);

        Vec2 next = start.stepTowards(new Vec2(5, 5), 0);

        assertEquals(start, next);
    }

    /** A negative step is rejected rather than moving away from the target. */
    @Test
    void stepTowardsRejectsNegativeStep() {
        Vec2 start = new Vec2(0, 0);

        assertThrows(IllegalArgumentException.class,
                () -> start.stepTowards(new Vec2(10, 0), -1));
    }

    /** Equal coordinates give equal vectors. */
    @Test
    void equalCoordinatesAreEqual() {
        assertEquals(new Vec2(1, 2), new Vec2(1, 2));
        assertEquals(new Vec2(1, 2).hashCode(), new Vec2(1, 2).hashCode());
    }
}
