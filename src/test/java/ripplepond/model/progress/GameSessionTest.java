package ripplepond.model.progress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import ripplepond.model.element.Frog;
import ripplepond.model.event.EventType;
import ripplepond.model.event.GameEvent;
import ripplepond.model.event.PondListener;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.io.LevelFormatException;
import ripplepond.model.io.LevelLoader;
import ripplepond.model.level.Level;
import ripplepond.model.level.TurnAction;
import ripplepond.model.level.Won;
import ripplepond.model.movement.Hop;

/**
 * Tests for {@link GameSession}: unlocking in order, a fresh frog for each level, and XP
 * and rank after a win.
 * <p>
 * Every level of the test session is {@code levels/quick-win.txt}, which one hop wins.
 * The hop costs 1.6 energy, so a rank 1 frog finishes with 10 HP and 18.4 energy and
 * earns 50 + 2 x 10 + 18 = 88 XP, enough for rank 2.
 */
class GameSessionTest {

    private static final String QUICK_WIN = "levels/quick-win.txt";
    private static final List<String> THREE_LEVELS = Collections.nCopies(3, QUICK_WIN);
    private static final TurnAction HOP_TO_FAR_BANK =
            new TurnAction(null, null, new Hop(), new Vec2(0.5, 0.29));

    /** Records every event the level publishes. */
    private static final class RecordingListener implements PondListener {
        final List<GameEvent> received = new ArrayList<>();

        @Override
        public void onEvent(GameEvent event) {
            received.add(event);
        }
    }

    private static GameSession sessionWith(FrogProgress progress) {
        return new GameSession(THREE_LEVELS, new LevelLoader(), progress);
    }

    private static Level win(Level level) {
        level.playTurn(HOP_TO_FAR_BANK);
        assertInstanceOf(Won.class, level.getState());
        return level;
    }

    // Unlocking

    @Test
    void theGameHasFiveLevelsAndOnlyTheFirstIsUnlocked() {
        GameSession session = new GameSession();

        assertEquals(5, session.levelCount());
        assertTrue(session.isUnlocked(0));
        for (int index = 1; index < 5; index++) {
            assertFalse(session.isUnlocked(index), "level " + index);
        }
        assertFalse(session.isUnlocked(-1));
        assertFalse(session.isUnlocked(5));
    }

    @Test
    void aLockedLevelCannotBeStarted() {
        GameSession session = sessionWith(new FrogProgress());

        assertThrows(IllegalStateException.class, () -> session.startLevel(1));
        assertThrows(IndexOutOfBoundsException.class, () -> session.startLevel(3));
    }

    @Test
    void winningALevelUnlocksTheNextOneOnly() throws LevelFormatException {
        GameSession session = sessionWith(new FrogProgress());

        session.completeLevel(win(session.startLevel(0)));

        assertTrue(session.isUnlocked(1));
        assertFalse(session.isUnlocked(2));
    }

    @Test
    void levelsUnlockInOrder() throws LevelFormatException {
        GameSession session = sessionWith(new FrogProgress());

        session.completeLevel(win(session.startLevel(0)));
        session.completeLevel(win(session.startLevel(1)));

        assertTrue(session.isUnlocked(2));
    }

    @Test
    void replayingAnEarlierLevelDoesNotLockOrSkipAnything() throws LevelFormatException {
        GameSession session = sessionWith(new FrogProgress());
        session.completeLevel(win(session.startLevel(0)));
        session.completeLevel(win(session.startLevel(1)));

        session.completeLevel(win(session.startLevel(0)));

        assertTrue(session.isUnlocked(2));
        assertEquals(0, session.getCurrentIndex());
    }

    @Test
    void winningTheLastLevelUnlocksNothingMore() throws LevelFormatException {
        GameSession session = new GameSession(List.of(QUICK_WIN), new LevelLoader(), new FrogProgress());

        session.completeLevel(win(session.startLevel(0)));

        assertTrue(session.isUnlocked(0));
        assertFalse(session.isUnlocked(1));
    }

    // Starting a level

    @Test
    void aLevelStartsWithAFreshFrogAtTheCurrentRank() throws LevelFormatException {
        GameSession session = sessionWith(new FrogProgress(120));

        Frog frog = session.startLevel(0).getPond().getFrog();

        assertEquals(Rank.RANK_3, frog.getRank());
        assertEquals(Rank.RANK_3.maxHealth(), frog.getHealth());
        assertEquals(Rank.RANK_3.maxEnergy(), frog.getEnergy(), 1e-12);
    }

    @Test
    void startingALevelAgainGivesANewCopy() throws LevelFormatException {
        GameSession session = sessionWith(new FrogProgress());
        Level first = session.startLevel(0);
        first.getPond().getFrog().takeDamage(5);

        Level second = session.startLevel(0);

        assertNotSame(first, second);
        assertEquals(10, second.getPond().getFrog().getHealth());
        assertEquals(0, second.getTurn());
    }

    // Completing a level

    @Test
    void completingALevelAwardsXpForTheFrogsHealthAndEnergy() throws LevelFormatException {
        GameSession session = sessionWith(new FrogProgress());

        int xp = session.completeLevel(win(session.startLevel(0)));

        assertEquals(88, xp);
        assertEquals(88, session.getProgress().getXp());
    }

    @Test
    void reachingANewRankIsAnnounced() throws LevelFormatException {
        GameSession session = sessionWith(new FrogProgress());
        Level level = win(session.startLevel(0));
        RecordingListener listener = new RecordingListener();
        level.addListener(listener);

        session.completeLevel(level);

        assertEquals(Rank.RANK_2, session.getProgress().getRank());
        assertEquals(1, listener.received.size());
        GameEvent event = listener.received.get(0);
        assertEquals(EventType.RANK_GAINED, event.getType());
        assertEquals(level.getPond().getFrog(), event.getSubject());
        assertEquals(2, event.getAmount(), 1e-12);
    }

    @Test
    void noRankGainNoAnnouncement() throws LevelFormatException {
        GameSession session = sessionWith(new FrogProgress(290));
        Level level = win(session.startLevel(0));
        RecordingListener listener = new RecordingListener();
        level.addListener(listener);

        session.completeLevel(level);

        assertTrue(listener.received.isEmpty());
        assertEquals(Rank.RANK_5, session.getProgress().getRank());
    }

    @Test
    void theNextLevelUsesTheNewRank() throws LevelFormatException {
        GameSession session = sessionWith(new FrogProgress());
        session.completeLevel(win(session.startLevel(0)));

        Frog frog = session.startLevel(1).getPond().getFrog();

        assertEquals(Rank.RANK_2, frog.getRank());
        assertEquals(12, frog.getHealth());
    }

    @Test
    void aLevelThatIsNotWonCannotBeCompleted() throws LevelFormatException {
        GameSession session = sessionWith(new FrogProgress());
        Level level = session.startLevel(0);

        assertThrows(IllegalStateException.class, () -> session.completeLevel(level));
        assertEquals(0, session.getProgress().getXp());
        assertFalse(session.isUnlocked(1));
    }

    @Test
    void aLevelCannotBeCompletedTwice() throws LevelFormatException {
        GameSession session = sessionWith(new FrogProgress());
        Level level = win(session.startLevel(0));
        session.completeLevel(level);

        assertThrows(IllegalStateException.class, () -> session.completeLevel(level));
        assertEquals(88, session.getProgress().getXp());
    }

    @Test
    void onlyTheLevelBeingPlayedCanBeCompleted() throws LevelFormatException {
        GameSession session = sessionWith(new FrogProgress());
        Level old = win(session.startLevel(0));
        session.startLevel(0);

        assertThrows(IllegalStateException.class, () -> session.completeLevel(old));
    }

    @Test
    void rejectsBadArguments() {
        assertThrows(IllegalArgumentException.class,
                () -> new GameSession(List.of(), new LevelLoader(), new FrogProgress()));
        assertThrows(NullPointerException.class,
                () -> new GameSession(null, new LevelLoader(), new FrogProgress()));
        assertThrows(NullPointerException.class, () -> sessionWith(new FrogProgress()).completeLevel(null));
    }
}
