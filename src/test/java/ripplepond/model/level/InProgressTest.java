package ripplepond.model.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import ripplepond.model.element.Frog;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.progress.Rank;

/**
 * Tests for {@link InProgress}: it runs the turn engine once per turn, then moves to the
 * state the end rules choose.
 * <p>
 * The real {@link TurnEngine} is S5, so the tests use a stand-in engine that records
 * the turn it was asked to run and does whatever the test says the turn should do.
 */
class InProgressTest {

    /** An engine that records each turn and applies a chosen effect to the frog. */
    private static final class StandInEngine extends TurnEngine {
        private int turnsRun;
        private Level lastLevel;
        private TurnAction lastAction;
        private Consumer<Level> effect = level -> { };

        @Override
        public void runTurn(Level level, TurnAction action) {
            turnsRun++;
            lastLevel = level;
            lastAction = action;
            effect.accept(level);
        }
    }

    private static final TurnAction NOTHING = new TurnAction(null, null, null, null);

    private final StandInEngine engine = new StandInEngine();
    private final InProgress inProgress = new InProgress(engine, new EndRules());
    private final Frog frog = new Frog(new Vec2(0.5, 0.25), Rank.RANK_1);
    private final FakePond pond = new FakePond(frog);
    private final Level level = new Level(pond, new ThrowBag(), 40, inProgress);

    @Test
    void aLevelInProgressIsNotOver() {
        assertFalse(inProgress.isOver());
        assertFalse(new InProgress().isOver());
    }

    @Test
    void playTurnRunsTheEngineOnceWithTheLevelAndTheAction() {
        inProgress.playTurn(level, NOTHING);

        assertEquals(1, engine.turnsRun);
        assertSame(level, engine.lastLevel);
        assertSame(NOTHING, engine.lastAction);
    }

    @Test
    void aQuietTurnKeepsTheSameState() {
        assertSame(inProgress, inProgress.playTurn(level, NOTHING));
    }

    @Test
    void aTurnThatEndsOnTheFarBankIsWon() {
        engine.effect = l -> pond.area = AreaType.FAR_BANK;

        assertInstanceOf(Won.class, inProgress.playTurn(level, NOTHING));
    }

    @Test
    void aTurnThatTakesTheLastHealthIsLost() {
        engine.effect = l -> frog.takeDamage(Rank.RANK_1.maxHealth());

        Lost lost = assertInstanceOf(Lost.class, inProgress.playTurn(level, NOTHING));
        assertEquals(LoseReason.NO_HEALTH, lost.getReason());
    }

    @Test
    void aTurnThatUsesTheLastEnergyWithoutReachingTheBankIsLost() {
        engine.effect = l -> frog.spendEnergy(Rank.RANK_1.maxEnergy());

        Lost lost = assertInstanceOf(Lost.class, inProgress.playTurn(level, NOTHING));
        assertEquals(LoseReason.NO_ENERGY, lost.getReason());
    }

    @Test
    void theStateAfterALossRefusesTheNextTurn() {
        engine.effect = l -> frog.takeDamage(Rank.RANK_1.maxHealth());
        LevelState after = inProgress.playTurn(level, NOTHING);

        assertThrows(IllegalStateException.class, () -> after.playTurn(level, NOTHING));
        assertEquals(1, engine.turnsRun);
    }

    @Test
    void rejectsNullArguments() {
        assertThrows(NullPointerException.class, () -> new InProgress(null, new EndRules()));
        assertThrows(NullPointerException.class, () -> new InProgress(engine, null));
        assertThrows(NullPointerException.class, () -> inProgress.playTurn(null, NOTHING));
        assertThrows(NullPointerException.class, () -> inProgress.playTurn(level, null));
        assertEquals(0, engine.turnsRun);
    }
}
