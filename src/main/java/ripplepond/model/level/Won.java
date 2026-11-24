package ripplepond.model.level;

/**
 * The state of a level the frog has won by reaching the far bank. A won level accepts
 * no more turns.
 */
public final class Won implements LevelState {

    /**
     * Refuses the turn: the level is already won.
     *
     * @param level  the level being played
     * @param action the refused action
     * @return never returns normally
     * @throws IllegalStateException always
     */
    @Override
    public LevelState playTurn(Level level, TurnAction action) {
        throw new IllegalStateException("the level is already won");
    }

    /**
     * Returns {@code true}: a won level is over.
     *
     * @return {@code true}
     */
    @Override
    public boolean isOver() {
        return true;
    }

    @Override
    public String toString() {
        return "Won";
    }
}
