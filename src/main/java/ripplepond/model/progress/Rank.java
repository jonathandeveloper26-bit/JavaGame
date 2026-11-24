package ripplepond.model.progress;

/**
 * The frog's rank, which grows with experience (GDD section 4.3).
 * <p>
 * Each rank is one row of the rank table, held here as data. A higher rank raises the
 * frog's maximum health and maximum energy. The ranks are declared from lowest to
 * highest, so their order is the order the frog reaches them.
 * <p>
 * This first version only holds the maximums, which {@code Frog} needs. The other
 * columns of the table (XP needed, hop range, swimming) and {@code forXp} are added
 * in J7.
 */
public enum Rank {

    /** The rank every frog starts at. */
    RANK_1(10, 20),

    /** Second rank. */
    RANK_2(12, 24),

    /** Third rank. */
    RANK_3(14, 28),

    /** Fourth rank. */
    RANK_4(16, 32),

    /** Fifth and highest rank. */
    RANK_5(18, 36);

    private final int maxHealth;
    private final int maxEnergy;

    /**
     * Creates a rank from one row of the rank table.
     *
     * @param maxHealth the most health a frog of this rank can have [proposed]
     * @param maxEnergy the most energy a frog of this rank can have [proposed]
     */
    Rank(int maxHealth, int maxEnergy) {
        this.maxHealth = maxHealth;
        this.maxEnergy = maxEnergy;
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
}
