package ripplepond.model.progress;

/**
 * The frog's experience across the whole game (GDD section 4.3).
 * <p>
 * A {@code Frog} is created fresh for every level, so its health and energy start full;
 * what carries over from level to level is kept here instead. Experience (XP) only ever
 * grows, and the rank always follows from it, so the two can never disagree.
 */
public class FrogProgress {

    private int xp;

    /**
     * Creates the progress of a new game: no XP, rank 1.
     */
    public FrogProgress() {
        this(0);
    }

    /**
     * Creates progress that starts with some XP, for example for a test that needs a
     * frog that can swim.
     *
     * @param xp the XP to start with; 0 or more
     * @throws IllegalArgumentException if {@code xp} is negative
     */
    public FrogProgress(int xp) {
        if (xp < 0) {
            throw new IllegalArgumentException("xp must be 0 or more: " + xp);
        }
        this.xp = xp;
    }

    /**
     * Adds XP, for example the award for a completed level. The total stops at the
     * largest whole number rather than wrapping round to a negative one.
     *
     * @param amount the XP to add; 0 or more
     * @throws IllegalArgumentException if {@code amount} is negative
     */
    public void addXp(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be 0 or more: " + amount);
        }
        xp = (int) Math.min(Integer.MAX_VALUE, (long) xp + amount);
    }

    /**
     * Returns the frog's total XP.
     *
     * @return the XP, 0 or more
     */
    public int getXp() {
        return xp;
    }

    /**
     * Returns the frog's rank, which follows from its XP.
     *
     * @return the rank for the current XP
     */
    public Rank getRank() {
        return Rank.forXp(xp);
    }
}
