package ripplepond.model.level;

/**
 * The state a level is in: still being played, won or lost (GDD section 3.2).
 * <p>
 * This is the State pattern. {@code Level.playTurn} hands each turn to its current
 * state and keeps the state it gets back. Only {@code InProgress} plays a turn; a won
 * or lost level refuses it. So the level needs no {@code if} chain to know whether a
 * turn is allowed: the answer comes from which state it is in.
 */
public interface LevelState {

    /**
     * Plays one turn in this state and returns the state the level is in afterwards.
     *
     * @param level  the level being played
     * @param action what the player asked for this turn
     * @return the state after the turn: still in progress, won or lost
     * @throws IllegalStateException if the level is already over
     */
    LevelState playTurn(Level level, TurnAction action);

    /**
     * Returns {@code true} when the level is won or lost and accepts no more turns.
     *
     * @return whether the level is over
     */
    boolean isOver();
}
