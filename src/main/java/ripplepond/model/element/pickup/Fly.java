package ripplepond.model.element.pickup;

import ripplepond.model.Balance;
import ripplepond.model.element.Frog;
import ripplepond.model.geometry.Vec2;

/**
 * A fly: restores some health and some energy (GDD 2.5).
 */
public final class Fly extends Food {

    /**
     * Creates a fly of the usual size, {@link Balance#FLY_SIZE}.
     *
     * @param position the centre of the fly, in play-area widths
     * @throws NullPointerException if {@code position} is {@code null}
     */
    public Fly(Vec2 position) {
        this(position, Balance.FLY_SIZE);
    }

    /**
     * Creates a fly with a chosen size, as a level file may give one.
     *
     * @param position the centre of the fly, in play-area widths
     * @param size     the diameter of the fly, in play-area widths; greater than 0
     * @throws NullPointerException     if {@code position} is {@code null}
     * @throws IllegalArgumentException if {@code size} is not greater than 0
     */
    public Fly(Vec2 position, double size) {
        super(position, size);
    }

    /**
     * Restores {@link Balance#FLY_HEALTH} health and {@link Balance#FLY_ENERGY} energy.
     *
     * @param frog the frog eating the fly
     */
    @Override
    protected void applyTo(Frog frog) {
        frog.restoreHealth(Balance.FLY_HEALTH);
        frog.restoreEnergy(Balance.FLY_ENERGY);
    }
}
