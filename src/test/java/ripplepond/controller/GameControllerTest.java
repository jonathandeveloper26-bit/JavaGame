package ripplepond.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import ripplepond.model.level.InProgress;
import ripplepond.model.level.Level;
import ripplepond.model.level.LoseReason;
import ripplepond.model.level.Lost;
import ripplepond.model.level.TurnAction;
import ripplepond.model.level.Won;
import ripplepond.model.movement.Hop;
import ripplepond.model.movement.Walk;
import ripplepond.model.progress.FrogProgress;
import ripplepond.model.progress.GameSession;
import ripplepond.model.progress.Rank;

/**
 * Tests for {@link GameController}, playing {@code levels/quick-win.txt} (one hop from
 * the start bank onto the far bank wins it; 5 turns allowed) through the real game.
 */
class GameControllerTest {

    private static final Vec2 START = new Vec2(0.5, 0.37);
    private static final TurnAction HOP_TO_FAR_BANK =
            new TurnAction(null, null, new Hop(), new Vec2(0.5, 0.29));
    private static final TurnAction WALK_ALONG_THE_BANK =
            new TurnAction(null, null, new Walk(), new Vec2(0.5, 0.43));
    private static final TurnAction NOTHING = new TurnAction(null, null, null, null);

    /** Records every event it hears. */
    private static final class RecordingListener implements PondListener {
        final List<GameEvent> received = new ArrayList<>();

        @Override
        public void onEvent(GameEvent event) {
            received.add(event);
        }

        List<EventType> types() {
            return received.stream().map(GameEvent::getType).toList();
        }
    }

    private static GameController controllerFor(int levels) {
        GameSession session = new GameSession(
                Collections.nCopies(levels, "levels/quick-win.txt"), new LevelLoader(), new FrogProgress());
        return new GameController(session);
    }

    // The task's finish line

    @Test
    void winningLevelOneThroughTheControllerChangesXpAndRank() throws LevelFormatException {
        GameController controller = controllerFor(3);
        RecordingListener listener = new RecordingListener();
        controller.addListener(listener);
        controller.startLevel(0);

        controller.submit(HOP_TO_FAR_BANK);

        assertInstanceOf(Won.class, controller.getLevel().getState());
        assertEquals(88, controller.getXpGained());
        assertEquals(88, controller.getSession().getProgress().getXp());
        assertEquals(Rank.RANK_2, controller.getSession().getProgress().getRank());
        assertEquals(List.of(EventType.FROG_MOVED, EventType.RANK_GAINED), listener.types());
    }

    // submit

    @Test
    void aTurnThatDoesNotWinAwardsNothing() throws LevelFormatException {
        GameController controller = controllerFor(3);
        controller.startLevel(0);

        controller.submit(WALK_ALONG_THE_BANK);

        assertInstanceOf(InProgress.class, controller.getLevel().getState());
        assertEquals(0, controller.getXpGained());
        assertEquals(0, controller.getSession().getProgress().getXp());
    }

    @Test
    void runningOutOfTurnsLosesAndAwardsNothing() throws LevelFormatException {
        GameController controller = controllerFor(3);
        controller.startLevel(0);

        for (int turn = 0; turn < 5; turn++) {
            controller.submit(NOTHING);
        }

        Lost lost = assertInstanceOf(Lost.class, controller.getLevel().getState());
        assertEquals(LoseReason.TURN_LIMIT, lost.getReason());
        assertEquals(0, controller.getSession().getProgress().getXp());
        assertFalse(controller.hasNextLevel());
    }

    @Test
    void aTurnBeforeAnyLevelOrAfterTheEndIsRefused() throws LevelFormatException {
        GameController controller = controllerFor(3);

        assertThrows(IllegalStateException.class, () -> controller.submit(NOTHING));

        controller.startLevel(0);
        controller.submit(HOP_TO_FAR_BANK);
        assertThrows(IllegalStateException.class, () -> controller.submit(NOTHING));
    }

    // reset

    @Test
    void resetRestoresTheLevelAsLoaded() throws LevelFormatException {
        GameController controller = controllerFor(3);
        controller.startLevel(0);
        Level before = controller.getLevel();
        controller.submit(WALK_ALONG_THE_BANK);

        controller.reset();

        Level after = controller.getLevel();
        Frog frog = after.getPond().getFrog();
        assertNotSame(before, after);
        assertEquals(0, after.getTurn());
        assertInstanceOf(InProgress.class, after.getState());
        assertEquals(START, frog.getPosition());
        assertEquals(Rank.RANK_1.maxEnergy(), frog.getEnergy(), 1e-12);
    }

    @Test
    void resetKeepsTheXpAlreadyEarned() throws LevelFormatException {
        GameController controller = controllerFor(3);
        controller.startLevel(0);
        controller.submit(HOP_TO_FAR_BANK);

        controller.reset();

        assertEquals(88, controller.getSession().getProgress().getXp());
        assertEquals(Rank.RANK_2, controller.getLevel().getPond().getFrog().getRank());
        assertEquals(0, controller.getXpGained());
    }

    @Test
    void aLostLevelCanBeReset() throws LevelFormatException {
        GameController controller = controllerFor(3);
        controller.startLevel(0);
        for (int turn = 0; turn < 5; turn++) {
            controller.submit(NOTHING);
        }

        controller.reset();

        assertInstanceOf(InProgress.class, controller.getLevel().getState());
    }

    @Test
    void listenersKeepHearingAfterAReset() throws LevelFormatException {
        GameController controller = controllerFor(3);
        RecordingListener listener = new RecordingListener();
        controller.addListener(listener);
        controller.startLevel(0);
        controller.reset();

        controller.submit(WALK_ALONG_THE_BANK);

        assertEquals(List.of(EventType.FROG_MOVED), listener.types());
    }

    @Test
    void aListenerAddedDuringALevelHearsItAtOnce() throws LevelFormatException {
        GameController controller = controllerFor(3);
        controller.startLevel(0);
        RecordingListener listener = new RecordingListener();
        controller.addListener(listener);

        controller.submit(WALK_ALONG_THE_BANK);

        assertEquals(List.of(EventType.FROG_MOVED), listener.types());
    }

    @Test
    void resetBeforeAnyLevelIsRefused() {
        assertThrows(IllegalStateException.class, () -> controllerFor(3).reset());
    }

    // nextLevel

    @Test
    void afterAWinTheNextLevelStartsAtTheNewRank() throws LevelFormatException {
        GameController controller = controllerFor(3);
        controller.startLevel(0);
        controller.submit(HOP_TO_FAR_BANK);

        assertTrue(controller.hasNextLevel());
        controller.nextLevel();

        assertEquals(1, controller.getSession().getCurrentIndex());
        assertEquals(Rank.RANK_2, controller.getLevel().getPond().getFrog().getRank());
        assertEquals(0, controller.getLevel().getTurn());
    }

    @Test
    void thereIsNoNextLevelBeforeAWin() throws LevelFormatException {
        GameController controller = controllerFor(3);
        controller.startLevel(0);

        assertFalse(controller.hasNextLevel());
        assertThrows(IllegalStateException.class, controller::nextLevel);
    }

    @Test
    void thereIsNoNextLevelAfterTheLastOne() throws LevelFormatException {
        GameController controller = controllerFor(1);
        controller.startLevel(0);
        controller.submit(HOP_TO_FAR_BANK);

        assertFalse(controller.hasNextLevel());
        assertThrows(IllegalStateException.class, controller::nextLevel);
    }

    @Test
    void beforeAnyLevelThereIsNothingToPlay() {
        GameController controller = controllerFor(3);

        assertNull(controller.getLevel());
        assertFalse(controller.hasNextLevel());
        assertEquals(0, controller.getXpGained());
    }

    @Test
    void rejectsNullArguments() throws LevelFormatException {
        assertThrows(NullPointerException.class, () -> new GameController(null));
        GameController controller = controllerFor(3);
        assertThrows(NullPointerException.class, () -> controller.addListener(null));
        controller.startLevel(0);
        assertThrows(NullPointerException.class, () -> controller.submit(null));
    }
}
