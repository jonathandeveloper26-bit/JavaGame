package ripplepond.model.element.pickup;

import ripplepond.model.Balance;
import ripplepond.model.element.Frog;
import ripplepond.model.geometry.Vec2;

/**
 * A breadcrumb: restores health only (GDD 2.5).
 */
public final class Breadcrumb extends Food {

    /**
     * Creates a breadcrumb of the usual size, {@link Balance#BREADCRUMB_SIZE}.
     *
     * @param position the centre of the breadcrumb, in play-area widths
     * @throws NullPointerException if {@code position} is {@code null}
     */
    public Breadcrumb(Vec2 position) {
        this(position, Balance.BREADCRUMB_SIZE);
    }

    /**
     * Creates a breadcrumb with a chosen size, as a level file may give one.
     *
     * @param position the centre of the breadcrumb, in play-area widths
     * @param size     the diameter of the breadcrumb, in play-area widths; greater than 0
     * @throws NullPointerException     if {@code position} is {@code null}
     * @throws IllegalArgumentException if {@code size} is not greater than 0
     */
    public Breadcrumb(Vec2 position, double size) {
        super(position, size);
    }

    /**
     * Restores {@link Balance#BREADCRUMB_HEALTH} health.
     *
     * @param frog the frog eating the breadcrumb
     */
    @Override
    protected void applyTo(Frog frog) {
        frog.restoreHealth(Balance.BREADCRUMB_HEALTH);
    }
}
