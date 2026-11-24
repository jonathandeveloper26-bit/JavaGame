package ripplepond.model.progress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import ripplepond.model.element.Frog;
import ripplepond.model.geometry.Vec2;

/**
 * Tests for {@link ExperienceRules}: XP for a level = 50 + 2 x HP left + 1 x energy left.
 */
class ExperienceRulesTest {

    private final ExperienceRules rules = new ExperienceRules();

    private static Frog frog(Rank rank) {
        return new Frog(new Vec2(0.5, 0.05), rank);
    }

    @Test
    void aFrogWithFullHealthAndEnergyEarnsTheMost() {
        // Rank 1: 50 + 2 x 10 + 1 x 20 = 90.
        assertEquals(90, rules.xpFor(frog(Rank.RANK_1)));
    }

    @Test
    void healthCountsTwiceAndEnergyOnce() {
        Frog frog = frog(Rank.RANK_1);
        frog.takeDamage(4);
        frog.spendEnergy(15);

        // 50 + 2 x 6 + 1 x 5 = 67.
        assertEquals(67, rules.xpFor(frog));
    }

    @Test
    void onlyWholePointsOfEnergyCount() {
        Frog frog = frog(Rank.RANK_1);
        frog.spendEnergy(12.1);

        // 7.9 energy left counts as 7: 50 + 20 + 7 = 77.
        assertEquals(77, rules.xpFor(frog));
    }

    @Test
    void aFrogThatJustMadeItStillEarnsTheFixedPart() {
        Frog frog = frog(Rank.RANK_1);
        frog.takeDamage(9);
        frog.spendEnergy(20);

        // 50 + 2 x 1 + 0 = 52.
        assertEquals(52, rules.xpFor(frog));
    }

    @Test
    void aHigherRankFrogCanEarnMore() {
        // Rank 3: 50 + 2 x 14 + 1 x 28 = 106.
        assertEquals(106, rules.xpFor(frog(Rank.RANK_3)));
    }

    @Test
    void aFullFirstLevelIsEnoughForRankTwo() {
        FrogProgress progress = new FrogProgress();

        progress.addXp(rules.xpFor(frog(Rank.RANK_1)));

        assertEquals(Rank.RANK_2, progress.getRank());
    }

    @Test
    void rejectsANullFrog() {
        assertThrows(NullPointerException.class, () -> rules.xpFor(null));
    }
}
