package ripplepond.view;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import ripplepond.controller.GameController;
import ripplepond.model.element.Frog;
import ripplepond.model.level.Level;
import ripplepond.model.level.LoseReason;
import ripplepond.model.level.Lost;

/**
 * What the game over screen shows about a lost level (GDD section 5): why it was lost,
 * in words a player understands, and how far the frog got.
 * <p>
 * It only reads the game; it holds no rules and no JavaFX, so it can be tested on its
 * own.
 */
final class LossSummary {

    private final LoseReason reason;
    private final int turnsUsed;
    private final int turnLimit;
    private final int health;
    private final double energy;

    private LossSummary(Level level, LoseReason reason) {
        Frog frog = level.getPond().getFrog();
        this.reason = reason;
        this.turnsUsed = level.getTurn();
        this.turnLimit = level.getTurnLimit();
        this.health = frog.getHealth();
        this.energy = frog.getEnergy();
    }

    /**
     * Reads the summary of the level the controller has just lost.
     *
     * @param controller the game controller, after a losing turn
     * @return the summary
     * @throws NullPointerException  if {@code controller} is {@code null}
     * @throws IllegalStateException if no level is loaded or the level is not lost
     */
    static LossSummary of(GameController controller) {
        Objects.requireNonNull(controller, "controller");
        Level level = controller.getLevel();
        if (level == null || !(level.getState() instanceof Lost lost)) {
            throw new IllegalStateException("only a lost level has a loss summary");
        }
        return new LossSummary(level, lost.getReason());
    }

    /**
     * Returns why the level was lost, as a player reads it.
     *
     * @param reason the reason from the {@code Lost} state
     * @return one sentence
     */
    static String explain(LoseReason reason) {
        return switch (Objects.requireNonNull(reason, "reason")) {
            case NO_HEALTH -> "The frog ran out of health.";
            case NO_ENERGY -> "The frog ran out of energy.";
            case TURN_LIMIT -> "You ran out of turns.";
        };
    }

    /**
     * Returns the lines the screen shows, in order: the reason first.
     *
     * @return the lines
     */
    List<String> lines() {
        return List.of(
                explain(reason),
                "Turns used: " + turnsUsed + " of " + turnLimit,
                "Health left: " + health,
                String.format(Locale.ROOT, "Energy left: %.1f", energy));
    }
}
