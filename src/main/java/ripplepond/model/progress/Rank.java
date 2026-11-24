package ripplepond.model.progress;

/**
 * The frog's rank, which grows with experience (GDD section 4.3).
 * <p>
 * Each rank is one row of the rank table, held here as data. A higher rank lets the frog
 * hop further and raises its maximum health and maximum energy, and from the third rank
 * the frog can swim. The ranks are declared from lowest to highest, so their order is
 * the order the frog reaches them.
 * <p>
 * The XP column of the table and {@code forXp} are added in J7.
 */
public enum Rank {

    /** The rank every frog starts at. */
    RANK_1(0.10, 10, 20, false),

    /** Second rank. */
    RANK_2(0.12, 12, 24, false),

    /** Third rank: the first that can swim. */
    RANK_3(0.14, 14, 28, true),

    /** Fourth rank. */
    RANK_4(0.16, 16, 32, true),

    /** Fifth and highest rank. */
    RANK_5(0.18, 18, 36, true);

    private final double hopRange;
    private final int maxHealth;
    private final int maxEnergy;
    private final boolean canSwim;

    /**
     * Creates a rank from one row of the rank table. Every number is [proposed].
     *
     * @param hopRange  the furthest a frog of this rank can hop, in play-area widths
     * @param maxHealth the most health a frog of this rank can have
     * @param maxEnergy the most energy a frog of this rank can have
     * @param canSwim   whether a frog of this rank can swim
     */
    Rank(double hopRange, int maxHealth, int maxEnergy, boolean canSwim) {
        this.hopRange = hopRange;
        this.maxHealth = maxHealth;
        this.maxEnergy = maxEnergy;
        this.canSwim = canSwim;
    }

    /**
     * Returns the furthest a frog of this rank can hop in one move.
     *
     * @return the hop range, in play-area widths
     */
    public double hopRange() {
        return hopRange;
    }

    /**
     * Returns the most health a frog of this rank can have. A frog starts each level
     * with this much health.
     *
     * @return the maximum health, in HP
     */
    public int maxHealth() {
        return maxHealth;
    }

    /**
     * Returns the most energy a frog of this rank can have. A frog starts each level
     * with this much energy.
     *
     * @return the maximum energy
     */
    public int maxEnergy() {
        return maxEnergy;
    }

    /**
     * Returns {@code true} if a frog of this rank can swim.
     *
     * @return whether swimming is unlocked
     */
    public boolean canSwim() {
        return canSwim;
    }
}
