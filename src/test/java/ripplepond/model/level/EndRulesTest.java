package ripplepond.model.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ripplepond.model.Balance;
import ripplepond.model.element.Frog;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.progress.Rank;

/**
 * Tests for {@link EndRules}: every rule in GDD section 3.2.
 * <p>
 * Two small stand-ins keep the tests independent of other tasks: a pond that reports
 * whichever area the test chooses (the real {@code Pond.areaAt} comes in S3), and a
 * level whose turn counter the test can set (it only goes up when a turn is played,
 * which needs the turn engine from S5).
 */
class EndRulesTest {

    private static final int TURN_LIMIT = 40;

    /** A pond whose every point is in the area the test chooses. */
    private static final class FakePond extends Pond {
        private AreaType area = AreaType.WATER;

        FakePond(Frog frog) {
            super(Balance.PLAY_AREA_WIDTH, Balance.PLAY_AREA_HEIGHT, frog);
        }

        @Override
        public AreaType areaAt(Vec2 point) {
            return area;
        }
    }

    /** A level whose turn counter the test can set. */
    private static final class FakeLevel extends Level {
        private int turn;

        FakeLevel(Pond pond, LevelState state) {
            super(pond, new ThrowBag(), TURN_LIMIT, state);
        }

        @Override
        public int getTurn() {
            return turn;
        }
    }

    /** The state the level is in while it is still being played. */
    private static final LevelState STILL_PLAYING = new LevelState() {
        @Override
        public LevelState playTurn(Level level, TurnAction action) {
            return this;
        }

        @Override
        public boolean isOver() {
            return false;
        }
    };

    private final EndRules rules = new EndRules();
    private final Frog frog = new Frog(new Vec2(0.5, 0.25), Rank.RANK_1);
    private final FakePond pond = new FakePond(frog);
    private final FakeLevel level = new FakeLevel(pond, STILL_PLAYING);

    private LoseReason lossReason() {
        return assertInstanceOf(Lost.class, rules.evaluate(level)).getReason();
    }

    // Still going

    @Test
    void aHealthyFrogInTheWaterBeforeTheLimitKeepsTheLevelGoing() {
        level.turn = 5;

        assertSame(STILL_PLAYING, rules.evaluate(level));
    }

    @Test
    void anEmptyThrowBagAloneDoesNotLose() {
        // The level's bag is empty; the frog can still walk or hop to the far bank.
        level.turn = TURN_LIMIT - 1;

        assertSame(STILL_PLAYING, rules.evaluate(level));
    }

    @Test
    void theFrogOnTheStartBankHasNotWon() {
        pond.area = AreaType.START_BANK;

        assertFalse(rules.hasWon(level));
        assertSame(STILL_PLAYING, rules.evaluate(level));
    }

    // Winning

    @Test
    void reachingTheFarBankWins() {
        pond.area = AreaType.FAR_BANK;

        assertTrue(rules.hasWon(level));
        assertInstanceOf(Won.class, rules.evaluate(level));
    }

    @Test
    void reachingTheFarBankWithTheLastEnergyStillWins() {
        pond.area = AreaType.FAR_BANK;
        frog.spendEnergy(Rank.RANK_1.maxEnergy());

        assertInstanceOf(Won.class, rules.evaluate(level));
    }

    @Test
    void reachingTheFarBankOnTheLastTurnStillWins() {
        pond.area = AreaType.FAR_BANK;
        level.turn = TURN_LIMIT;

        assertInstanceOf(Won.class, rules.evaluate(level));
    }

    // Losing

    @Test
    void noHealthLoses() {
        frog.takeDamage(Rank.RANK_1.maxHealth());

        assertEquals(LoseReason.NO_HEALTH, lossReason());
    }

    @Test
    void noEnergyLoses() {
        frog.spendEnergy(Rank.RANK_1.maxEnergy());

        assertEquals(LoseReason.NO_ENERGY, lossReason());
    }

    @Test
    void playingTheLastTurnAllowedLoses() {
        level.turn = TURN_LIMIT;

        assertEquals(LoseReason.TURN_LIMIT, lossReason());
    }

    @Test
    void theTurnBeforeTheLimitDoesNotLose() {
        level.turn = TURN_LIMIT - 1;

        assertFalse(rules.evaluate(level).isOver());
    }

    @Test
    void noHealthIsReportedBeforeNoEnergy() {
        frog.takeDamage(Rank.RANK_1.maxHealth());
        frog.spendEnergy(Rank.RANK_1.maxEnergy());
        level.turn = TURN_LIMIT;

        assertEquals(LoseReason.NO_HEALTH, lossReason());
    }

    @Test
    void noEnergyIsReportedBeforeTheTurnLimit() {
        frog.spendEnergy(Rank.RANK_1.maxEnergy());
        level.turn = TURN_LIMIT;

        assertEquals(LoseReason.NO_ENERGY, lossReason());
    }

    // Reading only

    @Test
    void evaluatingDoesNotChangeTheFrog() {
        frog.takeDamage(3);

        rules.evaluate(level);

        assertEquals(7, frog.getHealth());
        assertEquals(Rank.RANK_1.maxEnergy(), frog.getEnergy(), 1e-12);
    }

    @Test
    void rejectsANullLevel() {
        assertThrows(NullPointerException.class, () -> rules.evaluate(null));
        assertThrows(NullPointerException.class, () -> rules.hasWon(null));
    }
}
