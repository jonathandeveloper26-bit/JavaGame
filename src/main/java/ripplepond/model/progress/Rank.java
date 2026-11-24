package ripplepond.model.progress;

/**
 * The frog's rank, which grows with experience (GDD section 4.3).
 * <p>
 * Each rank is one row of the rank table, held here as data, in the table's column
 * order. A frog reaches a rank once its experience (XP) reaches that rank's threshold.
 * A higher rank lets the frog hop further and raises its maximum health and maximum
 * energy, and from the third rank the frog can swim. The ranks are declared from lowest
 * to highest, so their order is the order the frog reaches them.
 */
public enum Rank {

    /** The rank every frog starts at. */
    RANK_1(0, 0.10, 10, 20, false),

    /** Second rank. */
    RANK_2(50, 0.12, 12, 24, false),

    /** Third rank: the first that can swim. */
    RANK_3(120, 0.14, 14, 28, true),

    /** Fourth rank. */
    RANK_4(200, 0.16, 16, 32, true),

    /** Fifth and highest rank. */
    RANK_5(290, 0.18, 18, 36, true);

    private final int xpNeeded;
    private final double hopRange;
    private final int maxHealth;
    private final int maxEnergy;
    private final boolean canSwim;

    /**
     * Creates a rank from one row of the rank table. Every number is [proposed].
     *
     * @param xpNeeded  the XP a frog needs to reach this rank
     * @param hopRange  the furthest a frog of this rank can hop, in play-area widths
     * @param maxHealth the most health a frog of this rank can have
     * @param maxEnergy the most energy a frog of this rank can have
     * @param canSwim   whether a frog of this rank can swim
     */
    Rank(int xpNeeded, double hopRange, int maxHealth, int maxEnergy, boolean canSwim) {
        this.xpNeeded = xpNeeded;
        this.hopRange = hopRange;
        this.maxHealth = maxHealth;
        this.maxEnergy = maxEnergy;
        this.canSwim = canSwim;
    }

    /**
     * Returns the rank a frog has with a given amount of XP: the highest rank whose
     * threshold the XP has reached.
     *
     * @param xp the frog's experience; 0 or more
     * @return the rank for that much XP
     * @throws IllegalArgumentException if {@code xp} is negative
     */
    public static Rank forXp(int xp) {
        if (xp < 0) {
            throw new IllegalArgumentException("xp must be 0 or more: " + xp);
        }
        Rank reached = RANK_1;
        for (Rank rank : values()) {
            if (xp >= rank.xpNeeded) {
                reached = rank;
            }
        }
        return reached;
    }

    /**
     * Returns the XP a frog needs to reach this rank.
     *
     * @return the XP threshold; 0 for the first rank
     */
    public int xpNeeded() {
        return xpNeeded;
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
