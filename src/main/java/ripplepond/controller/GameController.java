package ripplepond.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ripplepond.model.event.PondListener;
import ripplepond.model.io.LevelFormatException;
import ripplepond.model.level.Level;
import ripplepond.model.level.TurnAction;
import ripplepond.model.level.Won;
import ripplepond.model.progress.GameSession;

/**
 * The one entry point the view uses to play the game (GDD sections 4.5 and 6).
 * <p>
 * The input handler builds a {@link TurnAction} from the player's clicks and hands it
 * to {@link #submit}; the buttons call {@link #reset} and {@link #nextLevel}. The view
 * holds no game rules: it reads the level from {@link #getLevel} and hears what happens
 * through the listeners added with {@link #addListener}.
 * <p>
 * Every time a level is loaded (at the start, after a reset, for the next level) the
 * level is a new object, so the controller attaches its listeners to it again. A won
 * level is completed straight away, so the XP and any new rank are known as soon as the
 * winning turn ends.
 */
public class GameController {

    private final GameSession session;
    private final List<PondListener> listeners = new ArrayList<>();
    private Level level;
    private int xpGained;

    /**
     * Creates a controller for a game. No level is loaded until {@link #startLevel}.
     *
     * @param session the game being played
     * @throws NullPointerException if {@code session} is {@code null}
     */
    public GameController(GameSession session) {
        this.session = Objects.requireNonNull(session, "session");
    }

    /**
     * Registers a listener for every event of every level this controller loads, now
     * and later.
     *
     * @param listener the listener to add, usually the level view
     * @throws NullPointerException if {@code listener} is {@code null}
     */
    public void addListener(PondListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
        if (level != null) {
            level.addListener(listener);
        }
    }

    /**
     * Loads an unlocked level and makes it the one being played.
     *
     * @param index the level, counting from 0
     * @throws IndexOutOfBoundsException if there is no such level
     * @throws IllegalStateException     if the level is still locked
     * @throws LevelFormatException      if the level file is missing or broken
     */
    public void startLevel(int index) throws LevelFormatException {
        Level loaded = session.startLevel(index);
        for (PondListener listener : listeners) {
            loaded.addListener(listener);
        }
        level = loaded;
        xpGained = 0;
    }

    /**
     * Plays one turn with the player's action. If the turn wins the level, the level is
     * completed at once: its XP is awarded and the next level unlocked.
     *
     * @param action what the player chose this turn
     * @throws NullPointerException  if {@code action} is {@code null}
     * @throws IllegalStateException if no level is loaded, or the level is already over
     */
    public void submit(TurnAction action) {
        Objects.requireNonNull(action, "action");
        requireLevel();
        level.playTurn(action);
        if (level.getState() instanceof Won) {
            xpGained = session.completeLevel(level);
        }
    }

    /**
     * Restarts the current level from its file: the river, the starting bag, and the
     * frog with full health and energy for its rank. Experience already earned is kept
     * (GDD 4.5). Works whether the level is in progress, won or lost.
     *
     * @throws IllegalStateException if no level is loaded
     * @throws LevelFormatException  if the level file can no longer be read
     */
    public void reset() throws LevelFormatException {
        requireLevel();
        startLevel(session.getCurrentIndex());
    }

    /**
     * Returns {@code true} if the current level is won and another level follows it.
     *
     * @return whether {@link #nextLevel} can be called
     */
    public boolean hasNextLevel() {
        return level != null
                && level.getState() instanceof Won
                && session.isUnlocked(session.getCurrentIndex() + 1);
    }

    /**
     * Starts the level after the one just won.
     *
     * @throws IllegalStateException if the current level is not won, or it was the last
     * @throws LevelFormatException  if the next level file is missing or broken
     */
    public void nextLevel() throws LevelFormatException {
        if (!hasNextLevel()) {
            throw new IllegalStateException("there is no next level to start");
        }
        startLevel(session.getCurrentIndex() + 1);
    }

    /**
     * Returns the level being played.
     *
     * @return the current level, or {@code null} before the first {@link #startLevel}
     */
    public Level getLevel() {
        return level;
    }

    /**
     * Returns the XP awarded when the current level was won, for the level complete
     * screen.
     *
     * @return the XP gained, or 0 while the level is not won
     */
    public int getXpGained() {
        return xpGained;
    }

    /**
     * Returns the game being played, for its XP, rank and unlocked levels.
     *
     * @return the session
     */
    public GameSession getSession() {
        return session;
    }

    private void requireLevel() {
        if (level == null) {
            throw new IllegalStateException("no level is loaded yet");
        }
    }
}
