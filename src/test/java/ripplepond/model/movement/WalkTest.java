package ripplepond.model.movement;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ripplepond.model.movement.MoveTestSupport.IN_WATER_NEAR_BANK;
import static ripplepond.model.movement.MoveTestSupport.MID_RIVER;
import static ripplepond.model.movement.MoveTestSupport.ON_START_BANK;
import static ripplepond.model.movement.MoveTestSupport.frogAt;
import static ripplepond.model.movement.MoveTestSupport.padAt;
import static ripplepond.model.movement.MoveTestSupport.pondFor;
import static ripplepond.model.movement.MoveTestSupport.reedAt;
import static ripplepond.model.movement.MoveTestSupport.standOnNewPad;

import org.junit.jupiter.api.Test;

import ripplepond.model.element.Frog;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.Pond;
import ripplepond.model.progress.Rank;

/**
 * Tests for {@link Walk}: across a bank, or within the object the frog stands on, up to
 * 0.10.
 */
class WalkTest {

    private final Walk walk = new Walk();

    @Test
    void walkingAlongTheBankIsLegal() {
        Frog frog = frogAt(ON_START_BANK, Rank.RANK_1);

        assertTrue(walk.isLegal(frog, new Vec2(0.5625, 0.625), pondFor(frog)));
    }

    @Test
    void walkingExactlyTheFurthestDistanceIsLegal() {
        Frog frog = frogAt(new Vec2(0.25, 0.625), Rank.RANK_1);

        assertTrue(walk.isLegal(frog, new Vec2(0.35, 0.625), pondFor(frog)));
    }

    @Test
    void walkingFurtherThanTheFurthestDistanceIsRefused() {
        Frog frog = frogAt(ON_START_BANK, Rank.RANK_5);

        assertFalse(walk.isLegal(frog, new Vec2(0.625, 0.625), pondFor(frog)));
    }

    @Test
    void walkingOffTheBankIntoTheWaterIsRefused() {
        Frog frog = frogAt(ON_START_BANK, Rank.RANK_1);

        assertFalse(walk.isLegal(frog, IN_WATER_NEAR_BANK, pondFor(frog)));
    }

    @Test
    void walkingOffTheBankOntoAnObjectIsRefused() {
        Frog frog = frogAt(ON_START_BANK, Rank.RANK_1);
        Pond pond = pondFor(frog);
        padAt(pond, IN_WATER_NEAR_BANK);

        // Getting onto an object from the bank is a hop.
        assertFalse(walk.isLegal(frog, IN_WATER_NEAR_BANK, pond));
    }

    @Test
    void walkingWithinTheObjectTheFrogStandsOnIsLegal() {
        Frog frog = frogAt(MID_RIVER, Rank.RANK_1);
        Pond pond = pondFor(frog);
        standOnNewPad(frog, pond);

        assertTrue(walk.isLegal(frog, new Vec2(0.53125, 0.375), pond));
    }

    @Test
    void walkingOffTheObjectIsRefused() {
        Frog frog = frogAt(MID_RIVER, Rank.RANK_1);
        Pond pond = pondFor(frog);
        standOnNewPad(frog, pond);

        // 0.09375 away: in walking range, but past the pad's edge at 0.0625.
        assertFalse(walk.isLegal(frog, new Vec2(0.5, 0.46875), pond));
    }

    @Test
    void walkingOntoANeighbouringObjectIsRefused() {
        Frog frog = frogAt(MID_RIVER, Rank.RANK_1);
        Pond pond = pondFor(frog);
        standOnNewPad(frog, pond);
        padAt(pond, new Vec2(0.5, 0.5));

        assertFalse(walk.isLegal(frog, new Vec2(0.5, 0.46875), pond));
    }

    @Test
    void aFrogInTheWaterCannotWalk() {
        Frog frog = frogAt(MID_RIVER, Rank.RANK_3);

        assertFalse(walk.isLegal(frog, new Vec2(0.53125, 0.375), pondFor(frog)));
    }

    @Test
    void aReedClumpInTheWayBlocksAWalk() {
        Frog frog = frogAt(ON_START_BANK, Rank.RANK_1);
        Pond pond = pondFor(frog);
        reedAt(pond, new Vec2(0.53125, 0.625));

        assertFalse(walk.isLegal(frog, new Vec2(0.5625, 0.625), pond));
    }
}
