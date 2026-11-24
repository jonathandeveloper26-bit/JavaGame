package ripplepond.model.element;

import ripplepond.model.geometry.Vec2;

/**
 * A turtle: it sleeps on the bottom of the river and blocks the space it occupies. It is
 * not an enemy (GDD section 2.2).
 * <p>
 * A turtle starts asleep. When a ripple's ring passes it, it wakes and moves away from
 * the ripple's source; that reaction is added in J8, by overriding {@code onRipple}.
 */
public final class Turtle extends AbstractElement {

    private boolean awake;

    /**
     * Creates a sleeping turtle.
     *
     * @param position the centre of the turtle, in play-area widths
     * @param size     the diameter of the turtle, in play-area widths; greater than 0
     * @throws NullPointerException     if {@code position} is {@code null}
     * @throws IllegalArgumentException if {@code size} is not greater than 0
     */
    public Turtle(Vec2 position, double size) {
        super(position, size);
        this.awake = false;
    }

    /**
     * Returns {@code true} once a ripple has woken the turtle.
     *
     * @return whether the turtle is awake
     */
    public boolean isAwake() {
        return awake;
    }

    /**
     * Returns {@code true}: a turtle blocks the space it occupies (GDD 2.2).
     *
     * @return {@code true}
     */
    @Override
    public boolean isSolid() {
        return true;
    }
}
