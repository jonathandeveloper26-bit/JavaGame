package ripplepond.model.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import ripplepond.model.element.Frog;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.progress.Rank;

/**
 * Tests for {@link Level#playTurn}: it counts the turn, hands it to the current state
 * and keeps the state the turn ends in, and a finished level refuses turns.
 * <p>
 * The level is played with {@link InProgress} and the real {@link EndRules}, but with
 * a stand-in engine whose turns do only what the test says, so the test controls when
 * the frog reaches the far bank.
 */
class LevelTest {

    private static final int TURN_LIMIT = 3;
    private static final TurnAction NOTHING = new TurnAction(null, null, null, null);

    /** An engine whose turns run a chosen effect and nothing else. */
    private static final class StandInEngine extends TurnEngine {
        private Runnable effect = () -> { };
        private int turnsRun;

        @Override
        public void runTurn(Level level, TurnAction action) {
            turnsRun++;
            effect.run();
        }
    }

    private final StandInEngine engine = new StandInEngine();
    private final InProgress inProgress = new InProgress(engine, new EndRules());
    private final Frog frog = new Frog(new Vec2(0.5, 0.5), Rank.RANK_1);
    private final FakePond pond = new FakePond(frog);
    private final Level level = new Level(pond, new ThrowBag(), TURN_LIMIT, inProgress);

    @Test
    void aNewLevelIsAtTurnZeroInTheStateItWasGiven() {
        assertEquals(0, level.getTurn());
        assertSame(inProgress, level.getState());
    }

    @Test
    void aQuietTurnIsCountedAndTheLevelGoesOn() {
        level.playTurn(NOTHING);

        assertEquals(1, level.getTurn());
        assertEquals(1, engine.turnsRun);
        assertSame(inProgress, level.getState());
    }

    @Test
    void aTurnThatReachesTheFarBankWinsTheLevel() {
        engine.effect = () -> pond.area = AreaType.FAR_BANK;

        level.playTurn(NOTHING);

        assertInstanceOf(Won.class, level.getState());
    }

    @Test
    void playingTheLastTurnAllowedWithoutReachingTheBankLoses() {
        for (int i = 0; i < TURN_LIMIT - 1; i++) {
            level.playTurn(NOTHING);
            assertFalse(level.getState().isOver(), "turn " + level.getTurn());
        }

        level.playTurn(NOTHING);

        Lost lost = assertInstanceOf(Lost.class, level.getState());
        assertEquals(LoseReason.TURN_LIMIT, lost.getReason());
        assertEquals(TURN_LIMIT, level.getTurn());
    }

    @Test
    void aFinishedLevelRefusesATurnAndDoesNotCountIt() {
        engine.effect = () -> pond.area = AreaType.FAR_BANK;
        level.playTurn(NOTHING);

        assertThrows(IllegalStateException.class, () -> level.playTurn(NOTHING));
        assertEquals(1, level.getTurn());
        assertEquals(1, engine.turnsRun);
    }

    @Test
    void rejectsNullArguments() {
        assertThrows(NullPointerException.class,
                () -> new Level(pond, new ThrowBag(), TURN_LIMIT, null));
        assertThrows(NullPointerException.class, () -> level.playTurn(null));
        assertEquals(0, level.getTurn());
    }
}
