package ripplepond.model.progress;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Rank}: the maximums match the rank table in GDD section 4.3.
 */
class RankTest {

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
