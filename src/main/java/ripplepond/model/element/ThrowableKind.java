package ripplepond.model.element;

import ripplepond.model.Balance;

/**
 * The kinds of object the player can throw (GDD section 2.2). Each kind has its own
 * size; a bigger object gives the frog more room to stand and walk.
 * <p>
 * The kinds are declared from smallest to largest.
 */
public enum ThrowableKind {

    /** A small object: room to stand, not to walk. */
    TWIG(Balance.TWIG_SIZE),

    /** A medium object: room for a short walk. */
    PLANK(Balance.PLANK_SIZE),

    /** A large object: room to walk along it. */
    LOG(Balance.LOG_SIZE);

    private final double size;

    ThrowableKind(double size) {
        this.size = size;
    }

    /**
     * Returns the diameter an object of this kind has when it is thrown.
     *
     * @return the diameter, in play-area widths
     */
    public double size() {
        return size;
    }
}
