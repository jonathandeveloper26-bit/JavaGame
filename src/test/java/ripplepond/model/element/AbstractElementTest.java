package ripplepond.model.element;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.Pond;
import ripplepond.model.ripple.Ripple;

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

    /** A ripple whose answer to {@code passes} is fixed, so reactTo can be tested alone. */
    private static final class FixedRipple extends Ripple {
        private final boolean passes;

        FixedRipple(boolean passes) {
            this.passes = passes;
        }

        @Override
        public boolean passes(Element element, Pond pond) {
            return passes;
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
        TestElement element = new TestElement(0.5, 0.5, 0.125);

        element.reactTo(new FixedRipple(true), null);

        assertEquals(1, element.ripplesFelt);
    }

    @Test
    void reactToDoesNothingWhenTheRingDoesNotPass() {
        TestElement element = new TestElement(0.5, 0.5, 0.125);

        element.reactTo(new FixedRipple(false), null);

        assertEquals(0, element.ripplesFelt);
    }
}
