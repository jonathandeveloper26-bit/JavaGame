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
 * Tests for {@link Hop}: from a surface to a surface, within the rank's hop range.
 */
class HopTest {

    private final Hop hop = new Hop();

    @Test
    void hoppingFromTheBankOntoAPadIsLegal() {
        Frog frog = frogAt(ON_START_BANK, Rank.RANK_1);
        Pond pond = pondFor(frog);
        padAt(pond, IN_WATER_NEAR_BANK);

        assertTrue(hop.isLegal(frog, IN_WATER_NEAR_BANK, pond));
    }

    @Test
    void hoppingIntoOpenWaterIsRefused() {
        Frog frog = frogAt(ON_START_BANK, Rank.RANK_5);

        assertFalse(hop.isLegal(frog, IN_WATER_NEAR_BANK, pondFor(frog)));
    }

    @Test
    void theHopRangeDependsOnTheRank() {
        // A pad 0.125 away: too far for rank 1 (0.10) and 2 (0.12), in range for rank 3 (0.14).
        Vec2 padCentre = new Vec2(0.5, 0.5);
        for (Rank rank : new Rank[] {Rank.RANK_1, Rank.RANK_2}) {
            Frog frog = frogAt(ON_START_BANK, rank);
            Pond pond = pondFor(frog);
            padAt(pond, padCentre);
            assertFalse(hop.isLegal(frog, padCentre, pond), rank.name());
        }
        Frog rankThree = frogAt(ON_START_BANK, Rank.RANK_3);
        Pond pond = pondFor(rankThree);
        padAt(pond, padCentre);
        assertTrue(hop.isLegal(rankThree, padCentre, pond));
    }

    @Test
    void hoppingFromAPadOntoTheFarBankIsLegal() {
        Frog frog = frogAt(new Vec2(0.5, 0.1875), Rank.RANK_1);
        Pond pond = pondFor(frog);
        standOnNewPad(frog, pond);

        assertTrue(hop.isLegal(frog, new Vec2(0.5, 0.09375), pond));
    }

    @Test
    void hoppingFromPadToPadIsLegal() {
        Frog frog = frogAt(MID_RIVER, Rank.RANK_1);
        Pond pond = pondFor(frog);
        standOnNewPad(frog, pond);
        padAt(pond, new Vec2(0.5, 0.28125));

        assertTrue(hop.isLegal(frog, new Vec2(0.5, 0.28125), pond));
    }

    @Test
    void aFrogInTheWaterCannotHop() {
        Frog frog = frogAt(MID_RIVER, Rank.RANK_5);
        Pond pond = pondFor(frog);
        padAt(pond, new Vec2(0.5, 0.28125));

        assertFalse(hop.isLegal(frog, new Vec2(0.5, 0.28125), pond));
    }

    @Test
    void aHopGoesOverAReedClump() {
        Frog frog = frogAt(ON_START_BANK, Rank.RANK_1);
        Pond pond = pondFor(frog);
        padAt(pond, IN_WATER_NEAR_BANK);
        reedAt(pond, new Vec2(0.5, 0.5625));

        assertTrue(hop.isLegal(frog, IN_WATER_NEAR_BANK, pond));
    }

    @Test
    void aHopCannotLandInsideAReedClump() {
        Frog frog = frogAt(new Vec2(0.5, 0.1875), Rank.RANK_1);
        Pond pond = pondFor(frog);
        standOnNewPad(frog, pond);
        reedAt(pond, new Vec2(0.5, 0.09375));

        assertFalse(hop.isLegal(frog, new Vec2(0.5, 0.09375), pond));
    }
}
