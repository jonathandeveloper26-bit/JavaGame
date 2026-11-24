package ripplepond.model.level;

import java.util.Objects;

/**
 * The state of a level the frog has lost, with the reason. A lost level accepts no more
 * turns; the player can only reset it (GDD section 4.5).
 */
public final class Lost implements LevelState {

    private final LoseReason reason;

    /**
     * Creates the lost state.
     *
     * @param reason why the level was lost
     * @throws NullPointerException if {@code reason} is {@code null}
     */
    public Lost(LoseReason reason) {
        this.reason = Objects.requireNonNull(reason, "reason");
    }

    /**
     * Returns why the level was lost, so the game over screen can say so.
     *
     * @return the reason
     */
    public LoseReason getReason() {
        return reason;
    }

    /**
     * Refuses the turn: the level is already lost.
     *
     * @param level  the level being played
     * @param action the refused action
     * @return never returns normally
     * @throws IllegalStateException always
     */
    @Override
    public LevelState playTurn(Level level, TurnAction action) {
        throw new IllegalStateException("the level is already lost: " + reason);
    }

    /**
     * Returns {@code true}: a lost level is over.
     *
     * @return {@code true}
     */
    @Override
    public boolean isOver() {
        return true;
    }

    @Override
    public String toString() {
        return "Lost(" + reason + ")";
    }
}
