package ripplepond.model.movement;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ripplepond.model.movement.MoveTestSupport.IN_WATER_NEAR_BANK;
import static ripplepond.model.movement.MoveTestSupport.MID_RIVER;
import static ripplepond.model.movement.MoveTestSupport.ON_START_BANK;
import static ripplepond.model.movement.MoveTestSupport.frogAt;
import static ripplepond.model.movement.MoveTestSupport.pondFor;
import static ripplepond.model.movement.MoveTestSupport.reedAt;

import org.junit.jupiter.api.Test;

import ripplepond.model.element.Frog;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.Pond;
import ripplepond.model.progress.Rank;

/**
 * Tests for {@link Swim}: through open water, up to 0.10, from rank 3.
 */
class SwimTest {

    private final Swim swim = new Swim();

    @Test
    void swimmingIsRefusedBeforeRankThree() {
        for (Rank rank : new Rank[] {Rank.RANK_1, Rank.RANK_2}) {
            Frog frog = frogAt(ON_START_BANK, rank);
            assertFalse(swim.isLegal(frog, IN_WATER_NEAR_BANK, pondFor(frog)), rank.name());
        }
    }

    @Test
    void divingInFromTheBankIsLegalFromRankThree() {
        Frog frog = frogAt(ON_START_BANK, Rank.RANK_3);

        assertTrue(swim.isLegal(frog, IN_WATER_NEAR_BANK, pondFor(frog)));
    }

    @Test
    void swimmingOnThroughTheWaterIsLegal() {
        Frog frog = frogAt(MID_RIVER, Rank.RANK_3);

        assertTrue(swim.isLegal(frog, new Vec2(0.5, 0.28125), pondFor(frog)));
    }

    @Test
    void climbingOutOntoTheFarBankIsLegal() {
        Frog frog = frogAt(new Vec2(0.5, 0.1875), Rank.RANK_3);

        assertTrue(swim.isLegal(frog, new Vec2(0.5, 0.09375), pondFor(frog)));
    }

    @Test
    void bankToBankIsAWalkNotASwim() {
        Frog frog = frogAt(ON_START_BANK, Rank.RANK_5);

        assertFalse(swim.isLegal(frog, new Vec2(0.5625, 0.625), pondFor(frog)));
    }

    @Test
    void swimmingFurtherThanTheFurthestDistanceIsRefused() {
        Frog frog = frogAt(MID_RIVER, Rank.RANK_5);

        assertFalse(swim.isLegal(frog, new Vec2(0.5, 0.25), pondFor(frog)));
    }

    @Test
    void aReedClumpInTheWayBlocksASwim() {
        Frog frog = frogAt(MID_RIVER, Rank.RANK_3);
        Pond pond = pondFor(frog);
        reedAt(pond, new Vec2(0.5, 0.328125));

        assertFalse(swim.isLegal(frog, new Vec2(0.5, 0.28125), pond));
    }
}
