package ripplepond.model.ripple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import ripplepond.model.Balance;
import ripplepond.model.element.Element;
import ripplepond.model.element.Frog;
import ripplepond.model.element.Reed;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.Pond;
import ripplepond.model.progress.Rank;

/**
 * Tests for the ripple rules in GDD sections 3.1 and 8.4: growth, fading, pushing once,
 * never pushing the origin, and the reed shadow.
 *
 * <p>Requires S3 (Pond) on main. The reed-shadow test is commented out until S3's
 * {@code Pond.reedBetween} and J2's {@code Reed} are merged.
 */
class RippleTest {

    private static final double DELTA = 1e-9;

    /** Advances the ripple by the given number of turns. */
    private static void advance(Ripple ripple, Pond pond, int turns) {
        for (int i = 0; i < turns; i++) {
            ripple.advance(pond);
        }
    }
    private static Pond emptyPond() {
        return new Pond(Pond.WIDTH, Pond.HEIGHT,
                new Frog(new Vec2(0, 0), Rank.RANK_1));
    }
    @Test
    void radiusGrowsByTheBalanceAmountEachTurn() {
        Vec2 source = new Vec2(0.5, 0.3);
        Ripple ripple = new Ripple(source, new StubElement(source, 0.04), new CircularSpread());
        Pond pond = emptyPond();

        ripple.advance(pond);
        assertEquals(Balance.RIPPLE_GROWTH_PER_TURN, ripple.getRadius(), DELTA);

        ripple.advance(pond);
        assertEquals(2 * Balance.RIPPLE_GROWTH_PER_TURN, ripple.getRadius(), DELTA);
    }

    @Test
    void rippleFadesAfterItsTurns() {
        Vec2 source = new Vec2(0.5, 0.3);
        Ripple ripple = new Ripple(source, new StubElement(source, 0.04), new CircularSpread());
        Pond pond = emptyPond();

        advance(ripple, pond, Balance.RIPPLE_LIFETIME_TURNS - 1);
        assertFalse(ripple.isFaded());

        ripple.advance(pond);
        assertTrue(ripple.isFaded());
    }

    @Test
    void fadedRippleDoesNotGrowAnyMore() {
        Vec2 source = new Vec2(0.5, 0.3);
        Ripple ripple = new Ripple(source, new StubElement(source, 0.04), new CircularSpread());
        Pond pond = emptyPond();
        advance(ripple, pond, Balance.RIPPLE_LIFETIME_TURNS);
        double radiusWhenFaded = ripple.getRadius();

        ripple.advance(pond);

        assertEquals(radiusWhenFaded, ripple.getRadius(), DELTA);
    }

    @Test
    void pushesAnElementOnceInTheTurnItsRingReachesIt() {
        // Source 0.2 above the element's centre; element radius 0.02, so its nearest edge is
        // 0.18 from the source. The ring reaches 0.16 after two turns and 0.24 after three.
        Vec2 source = new Vec2(0.5, 0.1);
        Ripple ripple = new Ripple(source, new StubElement(source, 0.04), new CircularSpread());
        Element element = new StubElement(new Vec2(0.5, 0.3), 0.04);
        Pond pond = emptyPond();

        ripple.advance(pond);
        assertFalse(ripple.passes(element, pond), "turn 1: ring has not reached the edge");
        ripple.advance(pond);
        assertFalse(ripple.passes(element, pond), "turn 2: ring has not reached the edge");
        ripple.advance(pond);
        assertTrue(ripple.passes(element, pond), "turn 3: ring reaches the edge");
        assertFalse(ripple.passes(element, pond), "the same element is never pushed twice");
    }

    @Test
    void neverPushesTheOriginElement() {
        Vec2 source = new Vec2(0.5, 0.3);
        Element origin = new StubElement(source, 0.04);
        Ripple ripple = new Ripple(source, origin, new CircularSpread());
        Pond pond = emptyPond();

        advance(ripple, pond, Balance.RIPPLE_LIFETIME_TURNS);

        assertFalse(ripple.passes(origin, pond));
    }

    @Test
    void elementCoveringTheSourceIsNeverReached() {
        // A large element whose nearest edge is already past the source.
        Vec2 source = new Vec2(0.5, 0.3);
        Ripple ripple = new Ripple(source, new StubElement(new Vec2(0.1, 0.1), 0.04), new CircularSpread());
        Element covering = new StubElement(new Vec2(0.5, 0.35), 0.4);
        Pond pond = emptyPond();

        advance(ripple, pond, Balance.RIPPLE_LIFETIME_TURNS);

        assertFalse(ripple.passes(covering, pond));
    }

    @Test
    void pushDirectionPointsFromSourceTowardsElementCentre() {
        Vec2 source = new Vec2(0.5, 0.3);
        Ripple ripple = new Ripple(source, new StubElement(new Vec2(0.1, 0.1), 0.04), new CircularSpread());
        Element element = new StubElement(new Vec2(0.5, 0.5), 0.04);

        Vec2 direction = ripple.pushDirection(element);

        assertEquals(0.0, direction.getX(), DELTA);
        assertEquals(1.0, direction.getY(), DELTA);
    }

    @Test
    void pushDirectionOfElementAtSourceIsZero() {
        Vec2 source = new Vec2(0.5, 0.3);
        Ripple ripple = new Ripple(source, new StubElement(new Vec2(0.1, 0.1), 0.04), new CircularSpread());
        Element atSource = new StubElement(source, 0.04);

        Vec2 direction = ripple.pushDirection(atSource);

        assertEquals(0.0, direction.getX(), DELTA);
        assertEquals(0.0, direction.getY(), DELTA);
    }


    @Test
    void reedClumpBetweenSourceAndElementShadowsIt() {
        Pond pond = emptyPond();
        pond.add(new Reed(new Vec2(0.5, 0.2), 0.06));
        Vec2 source = new Vec2(0.5, 0.1);
        Ripple ripple = new Ripple(source, new StubElement(source, 0.04), new CircularSpread());
        Element behindReed = new StubElement(new Vec2(0.5, 0.3), 0.04);
        advance(ripple, pond, Balance.RIPPLE_LIFETIME_TURNS);
        assertFalse(ripple.passes(behindReed, pond));
    }
    @Test
    void sameGeometryWithoutAReedReachesTheElement() {
        Pond pond = emptyPond();
        Vec2 source = new Vec2(0.5, 0.1);
        Ripple ripple = new Ripple(source, new StubElement(source, 0.04), new CircularSpread());
        Element behind = new StubElement(new Vec2(0.5, 0.3), 0.04);
        advance(ripple, pond, Balance.RIPPLE_LIFETIME_TURNS);
        assertTrue(ripple.passes(behind, pond));
    }
}