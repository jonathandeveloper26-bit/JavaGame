package ripplepond.model.element.pickup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ripplepond.model.element.Frog;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.Level;
import ripplepond.model.progress.Rank;

/**
 * Tests for {@link Pickup}. A pickup is an element the frog touches; what collecting it
 * does is up to each subclass, so the test uses one that records the call.
 */
class PickupTest {

    private static final double DELTA = 1e-12;

    /** A pickup that remembers which frog collected it. */
    private static final class TestPickup extends Pickup {
        private Frog collectedBy;

        TestPickup(Vec2 position, double size) {
            super(position, size);
        }

        TestPickup(double x, double y, double size) {
            this(new Vec2(x, y), size);
        }

        @Override
        public void collect(Frog frog, Level level) {
            collectedBy = frog;
        }
    }

    private final Frog frog = new Frog(new Vec2(0.5, 0.5), Rank.RANK_1);

    @Test
    void isAnElementWithAPositionAndASize() {
        Pickup pickup = new TestPickup(0.25, 0.5, 0.03125);

        assertEquals(new Vec2(0.25, 0.5), pickup.getPosition());
        assertEquals(0.03125, pickup.getSize(), DELTA);
    }

    @Test
    void theFrogReachesAPickupByTouchingIt() {
        // The frog's radius is 0.02 and this pickup's is 0.015625.
        Pickup near = new TestPickup(0.53, 0.5, 0.03125);
        Pickup far = new TestPickup(0.75, 0.5, 0.03125);

        assertTrue(frog.touches(near));
        assertFalse(frog.touches(far));
    }

    @Test
    void collectIsHandledByTheSubclass() {
        TestPickup pickup = new TestPickup(0.5, 0.5, 0.03125);

        pickup.collect(frog, null);

        assertSame(frog, pickup.collectedBy);
    }

    @Test
    void rejectsBadArguments() {
        assertThrows(NullPointerException.class, () -> new TestPickup(null, 0.03125));
        assertThrows(IllegalArgumentException.class, () -> new TestPickup(0.5, 0.5, 0));
    }
}
