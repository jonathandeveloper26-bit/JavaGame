package ripplepond.model.element;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.Pond;
import ripplepond.model.ripple.Ripple;
import ripplepond.model.ripple.CircularSpread;

/**
 * Tests for {@link AbstractElement}. The numbers are chosen to be exact in binary
 * (0.5, 0.25, ...) so that boundary cases are not blurred by rounding.
 */
class AbstractElementTest {

    private static final double DELTA = 1e-12;

    /** The smallest possible concrete element. It counts how often it felt a ripple. */
    private static final class TestElement extends AbstractElement {
        private int ripplesFelt;

        TestElement(Vec2 position, double size) {
            super(position, size);
        }

        TestElement(double x, double y, double size) {
            this(new Vec2(x, y), size);
        }

        @Override
        protected void onRipple(Ripple ripple, Pond pond) {
            ripplesFelt++;
        }

        void move(Vec2 target) {
            moveTo(target);
        }
    }



    @Test
    void radiusIsHalfTheSize() {
        TestElement element = new TestElement(0.5, 0.25, 0.5);

        assertEquals(0.5, element.getSize(), DELTA);
        assertEquals(0.25, element.getRadius(), DELTA);
    }

    @Test
    void distanceIsBetweenCentres() {
        TestElement a = new TestElement(0, 0, 0.5);
        TestElement b = new TestElement(0.75, 0, 0.25);

        assertEquals(0.75, a.distanceTo(b), DELTA);
        assertEquals(0.75, b.distanceTo(a), DELTA);
    }

    @Test
    void distanceIsTheSameInEveryDirection() {
        TestElement centre = new TestElement(0.5, 0.5, 0.125);
        TestElement right = new TestElement(0.75, 0.5, 0.125);
        TestElement below = new TestElement(0.5, 0.75, 0.125);

        assertEquals(centre.distanceTo(right), centre.distanceTo(below), DELTA);
    }

    @Test
    void elementsTouchWhenCentresAreExactlyTheSumOfTheRadiiApart() {
        // Radii 0.25 and 0.25, centres 0.5 apart: the circles meet at one point.
        TestElement a = new TestElement(0, 0, 0.5);
        TestElement b = new TestElement(0.5, 0, 0.5);

        assertTrue(a.touches(b));
        assertTrue(b.touches(a));
    }

    @Test
    void elementsTouchWhenTheyOverlap() {
        TestElement a = new TestElement(0, 0, 0.5);
        TestElement b = new TestElement(0.25, 0, 0.5);

        assertTrue(a.touches(b));
    }

    @Test
    void elementsDoNotTouchWhenSlightlyFurtherApart() {
        TestElement a = new TestElement(0, 0, 0.5);
        TestElement b = new TestElement(0.5 + 1e-9, 0, 0.5);

        assertFalse(a.touches(b));
    }

    @Test
    void moveToChangesThePosition() {
        TestElement element = new TestElement(0, 0, 0.5);

        element.move(new Vec2(0.25, 0.5));

        assertEquals(new Vec2(0.25, 0.5), element.getPosition());
    }

    @Test
    void moveToRejectsNull() {
        TestElement element = new TestElement(0, 0, 0.5);

        assertThrows(NullPointerException.class, () -> element.move(null));
    }

    @Test
    void constructorRejectsNullPosition() {
        assertThrows(NullPointerException.class, () -> new TestElement(null, 0.5));
    }

    @Test
    void constructorRejectsSizeZeroNegativeOrNaN() {
        assertThrows(IllegalArgumentException.class, () -> new TestElement(0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new TestElement(0, 0, -0.1));
        assertThrows(IllegalArgumentException.class, () -> new TestElement(0, 0, Double.NaN));
    }

    @Test
    void reactToCallsOnRippleWhenTheRingPasses() {
        Pond pond = new Pond(Pond.WIDTH, Pond.HEIGHT, null);
        Vec2 source = new Vec2(0.5, 0.5);
        Ripple ripple = new Ripple(source, new TestElement(source, 0.04), new CircularSpread());
        TestElement element = new TestElement(0.5, 0.7, 0.04);
        // The edge is 0.18 from the source; after three turns the ring has reached 0.24.
        for (int i = 0; i < 3; i++) {
            ripple.advance(pond);
        }

        element.reactTo(ripple, pond);

        assertEquals(1, element.ripplesFelt);
    }

    @Test
    void reactToDoesNothingWhenTheRingDoesNotPass() {
        Pond pond = new Pond(Pond.WIDTH, Pond.HEIGHT, null);
        Vec2 source = new Vec2(0.5, 0.5);
        Ripple ripple = new Ripple(source, new TestElement(source, 0.04), new CircularSpread());
        TestElement element = new TestElement(0.5, 0.7, 0.04);
        // No turn has run, so the ring has not moved.

        element.reactTo(ripple, pond);

        assertEquals(0, element.ripplesFelt);
    }
}
