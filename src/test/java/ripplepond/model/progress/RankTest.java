package ripplepond.model.progress;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Rank}: every column matches the rank table in GDD section 4.3.
 */
class RankTest {

    private static final double DELTA = 1e-12;

    @Test
    void hopRangesMatchTheRankTable() {
        double[] hop = {0.10, 0.12, 0.14, 0.16, 0.18};

        for (Rank rank : Rank.values()) {
            assertEquals(hop[rank.ordinal()], rank.hopRange(), DELTA, rank.name());
        }
    }

    @Test
    void eachRankHopsFurtherThanTheOneBefore() {
        Rank[] ranks = Rank.values();

        for (int i = 1; i < ranks.length; i++) {
            assertTrue(ranks[i].hopRange() > ranks[i - 1].hopRange(), ranks[i].name());
        }
    }

    @Test
    void swimmingStartsAtTheThirdRank() {
        assertFalse(Rank.RANK_1.canSwim());
        assertFalse(Rank.RANK_2.canSwim());
        assertTrue(Rank.RANK_3.canSwim());
        assertTrue(Rank.RANK_4.canSwim());
        assertTrue(Rank.RANK_5.canSwim());
    }

    @Test
    void thereAreFiveRanksFromLowestToHighest() {
        assertArrayEquals(
                new Rank[] {Rank.RANK_1, Rank.RANK_2, Rank.RANK_3, Rank.RANK_4, Rank.RANK_5},
                Rank.values());
    }

    @Test
    void theFirstRankHasTheStartingMaximums() {
        // GDD 4.1: the frog starts with 10 HP and 20 energy.
        assertEquals(10, Rank.RANK_1.maxHealth());
        assertEquals(20, Rank.RANK_1.maxEnergy());
    }

    @Test
    void maximumsMatchTheRankTable() {
        int[] health = {10, 12, 14, 16, 18};
        int[] energy = {20, 24, 28, 32, 36};

        for (Rank rank : Rank.values()) {
            assertEquals(health[rank.ordinal()], rank.maxHealth(), rank.name());
            assertEquals(energy[rank.ordinal()], rank.maxEnergy(), rank.name());
        }
    }

    @Test
    void eachRankRaisesBothMaximums() {
        Rank[] ranks = Rank.values();

        for (int i = 1; i < ranks.length; i++) {
            assertTrue(ranks[i].maxHealth() > ranks[i - 1].maxHealth(), ranks[i].name());
            assertTrue(ranks[i].maxEnergy() > ranks[i - 1].maxEnergy(), ranks[i].name());
        }
    }
}
