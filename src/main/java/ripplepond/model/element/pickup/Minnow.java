package ripplepond.model.element.pickup;

import ripplepond.model.Balance;
import ripplepond.model.element.Frog;
import ripplepond.model.geometry.Vec2;

/**
 * A small minnow: restores energy only (GDD 2.5).
 */
public final class Minnow extends Food {

    /**
     * Creates a minnow of the usual size, {@link Balance#MINNOW_SIZE}.
     *
     * @param position the centre of the minnow, in play-area widths
     * @throws NullPointerException if {@code position} is {@code null}
     */
    public Minnow(Vec2 position) {
        this(position, Balance.MINNOW_SIZE);
    }

    /**
     * Creates a minnow with a chosen size, as a level file may give one.
     *
     * @param position the centre of the minnow, in play-area widths
     * @param size     the diameter of the minnow, in play-area widths; greater than 0
     * @throws NullPointerException     if {@code position} is {@code null}
     * @throws IllegalArgumentException if {@code size} is not greater than 0
     */
    public Minnow(Vec2 position, double size) {
        super(position, size);
    }

    /**
     * Restores {@link Balance#MINNOW_ENERGY} energy.
     *
     * @param frog the frog eating the minnow
     */
    @Override
    protected void applyTo(Frog frog) {
        frog.restoreEnergy(Balance.MINNOW_ENERGY);
    }
}
