package ripplepond.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import ripplepond.controller.GameController;
import ripplepond.model.element.Frog;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.io.LevelFormatException;
import ripplepond.model.io.LevelLoader;
import ripplepond.model.level.LoseReason;
import ripplepond.model.level.TurnAction;
import ripplepond.model.movement.Hop;
import ripplepond.model.movement.Walk;
import ripplepond.model.progress.FrogProgress;
import ripplepond.model.progress.GameSession;

/**
 * Tests for {@link LossSummary}: each way of losing gets its own sentence, played for
 * real through the controller on {@code levels/quick-win.txt} (5 turns allowed).
 */
class LossSummaryTest {

    private static final TurnAction NOTHING = new TurnAction(null, null, null, null);
    private static final TurnAction SHORT_WALK = new TurnAction(null, null, new Walk(), new Vec2(0.5, 0.39));

    private static GameController started() throws LevelFormatException {
        GameController controller = new GameController(new GameSession(
                List.of("levels/quick-win.txt"), new LevelLoader(), new FrogProgress()));
        controller.startLevel(0);
        return controller;
    }

    @Test
    void runningOutOfTurns() throws LevelFormatException {
        GameController controller = started();
        for (int turn = 0; turn < 5; turn++) {
            controller.submit(NOTHING);
        }

        List<String> lines = LossSummary.of(controller).lines();

        assertEquals("You ran out of turns.", lines.get(0));
        assertEquals("Turns used: 5 of 5", lines.get(1));
        assertEquals("Health left: 10", lines.get(2));
        assertEquals("Energy left: 20.0", lines.get(3));
    }

    @Test
    void runningOutOfEnergy() throws LevelFormatException {
        GameController controller = started();
        Frog frog = controller.getLevel().getPond().getFrog();
        frog.spendEnergy(19.8);

        // A walk of 0.02 costs 0.2, the last of it.
        controller.submit(SHORT_WALK);

        assertEquals("The frog ran out of energy.", LossSummary.of(controller).lines().get(0));
    }

    @Test
    void runningOutOfHealth() throws LevelFormatException {
        GameController controller = started();
        controller.getLevel().getPond().getFrog().takeDamage(10);

        controller.submit(NOTHING);

        assertEquals("The frog ran out of health.", LossSummary.of(controller).lines().get(0));
    }

    @Test
    void everyReasonHasASentence() {
        for (LoseReason reason : LoseReason.values()) {
            String sentence = LossSummary.explain(reason);
            assertEquals('.', sentence.charAt(sentence.length() - 1), reason.name());
        }
    }

    @Test
    void onlyALostLevelHasALossSummary() throws LevelFormatException {
        GameController won = started();
        won.submit(new TurnAction(null, null, new Hop(), new Vec2(0.5, 0.29)));

        assertThrows(IllegalStateException.class, () -> LossSummary.of(won));
        assertThrows(IllegalStateException.class, () -> LossSummary.of(started()));
    }
}
