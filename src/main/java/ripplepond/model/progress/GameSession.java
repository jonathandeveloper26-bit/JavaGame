package ripplepond.model.progress;

import java.util.List;
import java.util.Objects;

import ripplepond.model.element.Frog;
import ripplepond.model.event.EventType;
import ripplepond.model.event.GameEvent;
import ripplepond.model.io.LevelFormatException;
import ripplepond.model.io.LevelLoader;
import ripplepond.model.level.Level;
import ripplepond.model.level.Won;

/**
 * One play-through of the game: the five levels in order, which of them are unlocked,
 * and the frog's experience (GDD sections 4.3 and 4.6).
 * <p>
 * Levels are numbered from 0 in the code; the screens can show them as "Level 1" to
 * "Level 5". Only the first level is unlocked at the start, and winning a level unlocks
 * the next one. Any unlocked level can be played again.
 * <p>
 * Each {@link #startLevel} loads the level file afresh, with a new frog at the current
 * rank, so health and energy are full. {@link #completeLevel} turns a win into XP; when
 * the XP lifts the frog to a new rank, the level announces {@code RANK_GAINED}.
 */
public class GameSession {

    /** The game's level files, in the order they are played. */
    public static final List<String> LEVEL_FILES = List.of(
            "levels/level1.txt",
            "levels/level2.txt",
            "levels/level3.txt",
            "levels/level4.txt",
            "levels/level5.txt");

    private final List<String> levelFiles;
    private final LevelLoader loader;
    private final FrogProgress progress;
    private final ExperienceRules rules = new ExperienceRules();
    private int unlockedLevels = 1;
    private int currentIndex = -1;
    private Level currentLevel;
    private boolean currentCompleted;

    /**
     * Creates a new game with the game's five levels and a frog with no XP.
     */
    public GameSession() {
        this(LEVEL_FILES, new LevelLoader(), new FrogProgress());
    }

    /**
     * Creates a game with chosen level files and progress. Tests use this to play small
     * test levels.
     *
     * @param levelFiles the level files, in order, as resource paths; at least one
     * @param loader     reads the level files
     * @param progress   the frog's experience so far
     * @throws NullPointerException     if an argument is {@code null}
     * @throws IllegalArgumentException if {@code levelFiles} is empty
     */
    public GameSession(List<String> levelFiles, LevelLoader loader, FrogProgress progress) {
        this.levelFiles = List.copyOf(Objects.requireNonNull(levelFiles, "levelFiles"));
        if (this.levelFiles.isEmpty()) {
            throw new IllegalArgumentException("a game needs at least one level");
        }
        this.loader = Objects.requireNonNull(loader, "loader");
        this.progress = Objects.requireNonNull(progress, "progress");
    }

    /**
     * Returns how many levels the game has.
     *
     * @return the number of levels
     */
    public int levelCount() {
        return levelFiles.size();
    }

    /**
     * Returns {@code true} if a level can be played.
     *
     * @param index the level, counting from 0
     * @return whether the level is unlocked; {@code false} for an index out of range
     */
    public boolean isUnlocked(int index) {
        return index >= 0 && index < unlockedLevels;
    }

    /**
     * Loads a level to play, with a fresh frog at the current rank. Starting a level
     * again (for example after a reset) gives a new copy, exactly as the file describes.
     *
     * @param index the level, counting from 0
     * @return the level, at turn 0 and in progress
     * @throws IndexOutOfBoundsException if there is no such level
     * @throws IllegalStateException     if the level is still locked
     * @throws LevelFormatException      if the level file is missing or broken
     */
    public Level startLevel(int index) throws LevelFormatException {
        Objects.checkIndex(index, levelFiles.size());
        if (!isUnlocked(index)) {
            throw new IllegalStateException("level " + (index + 1) + " is still locked");
        }
        Level level = loader.load(levelFiles.get(index), progress.getRank());
        currentIndex = index;
        currentLevel = level;
        currentCompleted = false;
        return level;
    }

    /**
     * Turns a won level into progress: awards its XP, unlocks the next level, and
     * publishes {@code RANK_GAINED} on the level when the frog reaches a new rank. The
     * event's subject is the frog and its amount is the new rank's number, 1 to 5.
     * <p>
     * Only the level most recently started can be completed, only once, and only if it
     * is won.
     *
     * @param level the level the player has just won
     * @return the XP awarded
     * @throws NullPointerException  if {@code level} is {@code null}
     * @throws IllegalStateException if the level is not the current one, is already
     *                               completed, or is not won
     */
    public int completeLevel(Level level) {
        Objects.requireNonNull(level, "level");
        if (level != currentLevel) {
            throw new IllegalStateException("only the level being played can be completed");
        }
        if (currentCompleted) {
            throw new IllegalStateException("this level has already been completed");
        }
        if (!(level.getState() instanceof Won)) {
            throw new IllegalStateException("only a won level can be completed: " + level.getState());
        }
        currentCompleted = true;
        Frog frog = level.getPond().getFrog();
        Rank before = progress.getRank();
        int xp = rules.xpFor(frog);
        progress.addXp(xp);
        unlockedLevels = Math.max(unlockedLevels, Math.min(currentIndex + 2, levelFiles.size()));
        Rank after = progress.getRank();
        if (after != before) {
            level.publish(new GameEvent(EventType.RANK_GAINED, frog, after.ordinal() + 1));
        }
        return xp;
    }

    /**
     * Returns the frog's experience.
     *
     * @return the progress, shared with the whole game
     */
    public FrogProgress getProgress() {
        return progress;
    }

    /**
     * Returns the level being played, counting from 0.
     *
     * @return the current level's index, or -1 before the first level is started
     */
    public int getCurrentIndex() {
        return currentIndex;
    }
}
