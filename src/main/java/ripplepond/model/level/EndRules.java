package ripplepond.model.level;

import java.util.Objects;

import ripplepond.model.element.Frog;

/**
 * Decides whether a level is won, lost or still going (GDD section 3.2).
 * <p>
 * The win is always checked first. The GDD says a move that reaches the far bank with
 * the last of the frog's energy still wins, and that predators do not act once the frog
 * has reached the bank, so nothing that happens after the frog arrives can turn a win
 * into a loss. The turn engine uses {@link #hasWon} to skip the predators' step on that
 * turn.
 * <p>
 * When several losses happen in the same turn, the reason reported is the first in this
 * order: no health, no energy, turn limit [proposed].
 * <p>
 * The rules only read the level; they never change it.
 */
public class EndRules {

    /**
     * Returns {@code true} when the frog is on the far bank.
     *
     * @param level the level being played
     * @return whether the level is won
     * @throws NullPointerException if {@code level} is {@code null}
     */
    public boolean hasWon(Level level) {
        Objects.requireNonNull(level, "level");
        Pond pond = level.getPond();
        return pond.areaAt(pond.getFrog().getPosition()) == AreaType.FAR_BANK;
    }

    /**
     * Returns the state the level is in after a turn.
     *
     * @param level the level being played, after the turn has run and its turn counter
     *              has gone up
     * @return {@link Won} if the frog is on the far bank; otherwise {@link Lost} with the
     *         first reason that applies; otherwise the level's current state, unchanged
     * @throws NullPointerException if {@code level} is {@code null}
     */
    public LevelState evaluate(Level level) {
        if (hasWon(level)) {
            return new Won();
        }
        Frog frog = level.getPond().getFrog();
        if (!frog.isAlive()) {
            return new Lost(LoseReason.NO_HEALTH);
        }
        if (!frog.hasEnergy()) {
            return new Lost(LoseReason.NO_ENERGY);
        }
        if (level.getTurn() >= level.getTurnLimit()) {
            return new Lost(LoseReason.TURN_LIMIT);
        }
        return level.getState();
    }
}
