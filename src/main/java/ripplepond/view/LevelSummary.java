package ripplepond.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import ripplepond.controller.GameController;
import ripplepond.model.element.Frog;
import ripplepond.model.level.Level;
import ripplepond.model.level.Won;
import ripplepond.model.progress.Rank;

/**
 * What the level complete screen shows about a won level (GDD section 5): turns used,
 * health and energy left, XP gained, and the new rank if the frog reached one.
 * <p>
 * It only reads the game; it holds no rules and no JavaFX, so it can be tested on its
 * own. A new rank shows when the rank the level was played at (the frog's) is lower
 * than the rank the frog's experience gives now.
 */
final class LevelSummary {

    private final int turnsUsed;
    private final int turnLimit;
    private final int health;
    private final int maxHealth;
    private final double energy;
    private final int maxEnergy;
    private final int xpGained;
    private final int totalXp;
    private final Rank rankPlayed;
    private final Rank rankNow;

    private LevelSummary(Level level, int xpGained, int totalXp, Rank rankNow) {
        Frog frog = level.getPond().getFrog();
        this.turnsUsed = level.getTurn();
        this.turnLimit = level.getTurnLimit();
        this.health = frog.getHealth();
        this.maxHealth = frog.getRank().maxHealth();
        this.energy = frog.getEnergy();
        this.maxEnergy = frog.getRank().maxEnergy();
        this.xpGained = xpGained;
        this.totalXp = totalXp;
        this.rankPlayed = frog.getRank();
        this.rankNow = rankNow;
    }

    /**
     * Reads the summary of the level the controller has just finished.
     *
     * @param controller the game controller, after a winning turn
     * @return the summary
     * @throws NullPointerException  if {@code controller} is {@code null}
     * @throws IllegalStateException if no level is loaded or the level is not won
     */
    static LevelSummary of(GameController controller) {
        Objects.requireNonNull(controller, "controller");
        Level level = controller.getLevel();
        if (level == null || !(level.getState() instanceof Won)) {
            throw new IllegalStateException("only a won level has a summary");
        }
        return new LevelSummary(level, controller.getXpGained(),
                controller.getSession().getProgress().getXp(),
                controller.getSession().getProgress().getRank());
    }

    /**
     * Returns the rank the frog has just reached, if this level lifted it.
     *
     * @return the new rank, or an empty {@code Optional}
     */
    Optional<Rank> newRank() {
        return rankNow.compareTo(rankPlayed) > 0 ? Optional.of(rankNow) : Optional.empty();
    }

    /**
     * Returns the lines the screen shows, in order.
     *
     * @return one line per fact, plus one for a new rank when there is one
     */
    List<String> lines() {
        List<String> lines = new ArrayList<>();
        lines.add("Turns used: " + turnsUsed + " of " + turnLimit);
        lines.add("Health left: " + health + " / " + maxHealth);
        lines.add(String.format(Locale.ROOT, "Energy left: %.1f / %d", energy, maxEnergy));
        lines.add("XP gained: " + xpGained + " (total " + totalXp + ")");
        newRank().ifPresent(rank -> lines.add("New rank: " + rankName(rank) + "!"));
        return lines;
    }

    /**
     * Returns a rank as players read it.
     *
     * @param rank the rank
     * @return for example "Rank 2"
     */
    static String rankName(Rank rank) {
        return "Rank " + (rank.ordinal() + 1);
    }
}
