package ripplepond.model.movement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ripplepond.model.movement.MoveTestSupport.ON_START_BANK;
import static ripplepond.model.movement.MoveTestSupport.frogAt;
import static ripplepond.model.movement.MoveTestSupport.pondFor;

import org.junit.jupiter.api.Test;

import ripplepond.model.element.Frog;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.Pond;
import ripplepond.model.progress.Rank;

/**
 * Tests for what every move mode shares: costs by distance, the 1, 2 and 4 ratio, the
 * furthest distances, and the checks in {@link AbstractMoveMode#isLegal}.
 */
class MoveModeTest {

    private static final double DELTA = 1e-12;

    private final MoveMode walk = new Walk();
    private final MoveMode hop = new Hop();
    private final MoveMode swim = new Swim();

    @Test
    void movingTheBaseDistanceCostsOneTwoAndFour() {
        assertEquals(1, walk.costFor(0.10), DELTA);
        assertEquals(2, hop.costFor(0.10), DELTA);
        assertEquals(4, swim.costFor(0.10), DELTA);
    }

    @Test
    void theCostGrowsInProportionToTheDistance() {
        assertEquals(0.5, walk.costFor(0.05), DELTA);
        assertEquals(1.5, walk.costFor(0.15), DELTA);
        assertEquals(2 * hop.costFor(0.06), hop.costFor(0.12), DELTA);
        assertEquals(0, swim.costFor(0), DELTA);
    }

    @Test
    void overTheSameDistanceHoppingCostsTwiceAWalkAndSwimmingFourTimes() {
        double distance = 0.0625;

        assertEquals(2 * walk.costFor(distance), hop.costFor(distance), DELTA);
        assertEquals(4 * walk.costFor(distance), swim.costFor(distance), DELTA);
    }

    @Test
    void aNegativeOrNaNDistanceHasNoCost() {
        assertThrows(IllegalArgumentException.class, () -> walk.costFor(-0.1));
        assertThrows(IllegalArgumentException.class, () -> walk.costFor(Double.NaN));
    }

    @Test
    void furthestDistances() {
        Frog rankOne = frogAt(ON_START_BANK, Rank.RANK_1);
        Frog rankFive = frogAt(ON_START_BANK, Rank.RANK_5);

        assertEquals(0.10, walk.maxDistance(rankOne), DELTA);
        assertEquals(0.10, swim.maxDistance(rankOne), DELTA);
        assertEquals(0.10, hop.maxDistance(rankOne), DELTA);
        assertEquals(0.18, hop.maxDistance(rankFive), DELTA);
    }

    @Test
    void aMoveToWhereTheFrogAlreadyIsIsRefused() {
        Frog frog = frogAt(ON_START_BANK, Rank.RANK_5);
        Pond pond = pondFor(frog);

        assertFalse(walk.isLegal(frog, ON_START_BANK, pond));
        assertFalse(hop.isLegal(frog, ON_START_BANK, pond));
        assertFalse(swim.isLegal(frog, ON_START_BANK, pond));
    }

    @Test
    void aMoveOutsideThePlayAreaIsRefused() {
        Frog frog = frogAt(new Vec2(0.03125, 0.625), Rank.RANK_5);
        Pond pond = pondFor(frog);

        assertFalse(walk.isLegal(frog, new Vec2(-0.03125, 0.625), pond));
        assertFalse(walk.isLegal(frog, new Vec2(0.03125, 0.6875), pond));
    }

    @Test
    void isLegalRejectsNulls() {
        Frog frog = frogAt(ON_START_BANK, Rank.RANK_1);
        Pond pond = pondFor(frog);

        assertThrows(NullPointerException.class, () -> walk.isLegal(null, ON_START_BANK, pond));
        assertThrows(NullPointerException.class, () -> walk.isLegal(frog, null, pond));
        assertThrows(NullPointerException.class, () -> walk.isLegal(frog, ON_START_BANK, null));
    }
}
