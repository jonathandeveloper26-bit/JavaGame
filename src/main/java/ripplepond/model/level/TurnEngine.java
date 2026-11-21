package ripplepond.model.level;

/**
 * Runs one turn as five steps in a fixed order. The step methods are filled in
 * across S5 and S6.
 */
public class TurnEngine {

    /**
     * Runs the five steps of a turn in order. Implemented in S5.
     *
     * @param level  the level being played
     * @param action what the player asked for this turn
     * @throws UnsupportedOperationException until S5 is merged
     */
    public void runTurn(Level level, TurnAction action) {
        throw new UnsupportedOperationException("Implemented in S5");
    }

    /**
     * Step 1: places the thrown object and starts its ripple. Implemented in S5.
     *
     * @param level  the level being played
     * @param action the action containing the throw
     * @throws UnsupportedOperationException until S5 is merged
     */
    protected void throwStep(Level level, TurnAction action) {
        throw new UnsupportedOperationException("Implemented in S5");
    }

    /**
     * Step 2: grows every active ripple. Implemented in S5.
     *
     * @param pond the pond holding the ripples
     * @throws UnsupportedOperationException until S5 is merged
     */
    protected void spreadRipples(Pond pond) {
        throw new UnsupportedOperationException("Implemented in S5");
    }

    /**
     * Step 3: lets each element react to the ripples. Implemented in S5.
     *
     * @param pond the pond holding the elements and ripples
     * @throws UnsupportedOperationException until S5 is merged
     */
    protected void objectsReact(Pond pond) {
        throw new UnsupportedOperationException("Implemented in S5");
    }

    /**
     * Step 4: moves the frog if the action includes a move. Implemented in S5.
     *
     * @param level  the level being played
     * @param action the action containing the move
     * @throws UnsupportedOperationException until S5 is merged
     */
    protected void frogMoves(Level level, TurnAction action) {
        throw new UnsupportedOperationException("Implemented in S5");
    }

    /**
     * Step 5: predators move and attack. Implemented in S6.
     *
     * @param level the level being played
     * @throws UnsupportedOperationException until S6 is merged
     */
    protected void predatorsAct(Level level) {
        throw new UnsupportedOperationException("Implemented in S6");
    }
}