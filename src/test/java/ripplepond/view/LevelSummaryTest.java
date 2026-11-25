package ripplepond.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import ripplepond.controller.GameController;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.io.LevelFormatException;
import ripplepond.model.io.LevelLoader;
import ripplepond.model.level.TurnAction;
import ripplepond.model.movement.Hop;
import ripplepond.model.movement.Walk;
import ripplepond.model.progress.FrogProgress;
import ripplepond.model.progress.GameSession;
import ripplepond.model.progress.Rank;

/**
 * Tests for {@link LevelSummary}, on the one-hop test level {@code levels/quick-win.txt}
 * (5 turns allowed). The winning hop costs 1.6 energy, so a rank 1 frog ends with
 * 10 HP and 18.4 energy and earns 88 XP, enough for rank 2.
 */
class LevelSummaryTest {

    private static final TurnAction WINNING_HOP = new TurnAction(null, null, new Hop(), new Vec2(0.5, 0.29));

    private static GameController wonAtXp(int startingXp) throws LevelFormatException {
        GameController controller = controllerAtXp(startingXp);
        controller.submit(WINNING_HOP);
        return controller;
    }

    private static GameController controllerAtXp(int startingXp) throws LevelFormatException {
        GameSession session = new GameSession(List.of("levels/quick-win.txt", "levels/quick-win.txt"),
                new LevelLoader(), new FrogProgress(startingXp));
        GameController controller = new GameController(session);
        controller.startLevel(0);
        return controller;
    }

    @Test
    void theSummaryShowsTurnsHealthEnergyAndXp() throws LevelFormatException {
        List<String> lines = LevelSummary.of(wonAtXp(0)).lines();

        assertEquals("Turns used: 1 of 5", lines.get(0));
        assertEquals("Health left: 10 / 10", lines.get(1));
        assertEquals("Energy left: 18.4 / 20", lines.get(2));
        assertEquals("XP gained: 88 (total 88)", lines.get(3));
    }

    @Test
    void aNewRankIsShownWhenTheLevelLiftsTheFrog() throws LevelFormatException {
        LevelSummary summary = LevelSummary.of(wonAtXp(0));

        assertEquals(Rank.RANK_2, summary.newRank().orElseThrow());
        assertEquals("New rank: Rank 2!", summary.lines().get(4));
    }

    @Test
    void noNewRankLineWhenTheRankStaysTheSame() throws LevelFormatException {
        // Rank 5 already: there is no higher rank to reach.
        LevelSummary summary = LevelSummary.of(wonAtXp(290));

        assertTrue(summary.newRank().isEmpty());
        assertEquals(4, summary.lines().size());
    }

    @Test
    void theHealthAndEnergyMaximumsAreTheRankTheLevelWasPlayedAt() throws LevelFormatException {
        // Played at rank 3 (14 HP, 28 energy).
        List<String> lines = LevelSummary.of(wonAtXp(120)).lines();

        assertEquals("Health left: 14 / 14", lines.get(1));
        assertEquals("Energy left: 26.4 / 28", lines.get(2));
    }

    @Test
    void onlyAWonLevelHasASummary() throws LevelFormatException {
        GameController notWon = controllerAtXp(0);
        notWon.submit(new TurnAction(null, null, new Walk(), new Vec2(0.5, 0.45)));

        assertThrows(IllegalStateException.class, () -> LevelSummary.of(notWon));
        assertThrows(IllegalStateException.class,
                () -> LevelSummary.of(new GameController(new GameSession())));
    }

    @Test
    void ranksAreNumberedFromOneForPlayers() {
        assertEquals("Rank 1", LevelSummary.rankName(Rank.RANK_1));
        assertEquals("Rank 5", LevelSummary.rankName(Rank.RANK_5));
    }
}
