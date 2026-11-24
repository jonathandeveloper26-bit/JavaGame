package ripplepond.model.progress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link FrogProgress}: XP only grows and the rank follows it.
 */
class FrogProgressTest {

    @Test
    void aNewGameStartsWithNoXpAtRankOne() {
        FrogProgress progress = new FrogProgress();

        assertEquals(0, progress.getXp());
        assertEquals(Rank.RANK_1, progress.getRank());
    }

    @Test
    void addXpAddsUp() {
        FrogProgress progress = new FrogProgress();

        progress.addXp(30);
        progress.addXp(12);

        assertEquals(42, progress.getXp());
    }

    @Test
    void theRankFollowsTheXp() {
        FrogProgress progress = new FrogProgress();

        progress.addXp(49);
        assertEquals(Rank.RANK_1, progress.getRank());

        progress.addXp(1);
        assertEquals(Rank.RANK_2, progress.getRank());

        progress.addXp(70);
        assertEquals(Rank.RANK_3, progress.getRank());
    }

    @Test
    void progressCanStartWithSomeXp() {
        FrogProgress progress = new FrogProgress(200);

        assertEquals(200, progress.getXp());
        assertEquals(Rank.RANK_4, progress.getRank());
    }

    @Test
    void xpStopsAtTheLargestWholeNumberInsteadOfWrapping() {
        FrogProgress progress = new FrogProgress(Integer.MAX_VALUE - 1);

        progress.addXp(10);

        assertEquals(Integer.MAX_VALUE, progress.getXp());
        assertEquals(Rank.RANK_5, progress.getRank());
    }

    @Test
    void negativeXpIsRefused() {
        FrogProgress progress = new FrogProgress(10);

        assertThrows(IllegalArgumentException.class, () -> progress.addXp(-1));
        assertThrows(IllegalArgumentException.class, () -> new FrogProgress(-1));
        assertEquals(10, progress.getXp());
    }
}
