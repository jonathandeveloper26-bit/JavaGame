package ripplepond.model.element;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.InProgress;
import ripplepond.model.level.Level;
import ripplepond.model.level.Pond;
import ripplepond.model.level.ThrowBag;
import ripplepond.model.progress.Rank;

/**
 * Tests for {@link AbstractElement}. The numbers are chosen to be exact in binary
 * (0.5, 0.25, ...) so that boundary cases are not blurred by rounding.
 */
class AbstractElementTest {

    private static final double DELTA = 1e-12;

    /** The smallest possible concrete element. It records the pushes it felt. */
    private static final class TestElement extends AbstractElement {
        private int ripplesFelt;
        private Vec2 lastPush;

        TestElement(Vec2 position, double size) {
            super(position, size);
        }

        TestElement(double x, double y, double size) {
            this(new Vec2(x, y), size);
        }

        @Override
        protected void onRipple(Vec2 push, Level level) {
            ripplesFelt++;
            lastPush = push;
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

    private static Level level() {
        Frog frog = new Frog(new Vec2(0.5, 0.5), Rank.RANK_1);
        return new Level(new Pond(Pond.WIDTH, Pond.HEIGHT, frog), new ThrowBag(), 40, new InProgress());
    }

    @Test
    void reactToHandsThePushToOnRipple() {
        TestElement element = new TestElement(0.5, 0.25, 0.125);
        Vec2 push = new Vec2(0, 0.0625);

        element.reactTo(push, level());

        assertEquals(1, element.ripplesFelt);
        assertEquals(push, element.lastPush);
    }

    @Test
    void aZeroPushStillReachesOnRipple() {
        // Ripples that cancel out still passed the element, so it is still asked.
        TestElement element = new TestElement(0.5, 0.25, 0.125);

        element.reactTo(new Vec2(0, 0), level());

        assertEquals(1, element.ripplesFelt);
    }

    @Test
    void byDefaultAnElementIgnoresRipples() {
        Reed reed = new Reed(new Vec2(0.5, 0.25), 0.125);

        reed.reactTo(new Vec2(0.0625, 0), level());

        assertEquals(new Vec2(0.5, 0.25), reed.getPosition());
    }

    @Test
    void reactToRejectsNulls() {
        TestElement element = new TestElement(0.5, 0.25, 0.125);

        assertThrows(NullPointerException.class, () -> element.reactTo(null, level()));
        assertThrows(NullPointerException.class, () -> element.reactTo(new Vec2(0, 0), null));
        assertEquals(0, element.ripplesFelt);
    }
}
