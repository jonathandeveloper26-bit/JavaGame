package ripplepond.model.element;

import java.util.Objects;

import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.Pond;
import ripplepond.model.ripple.Ripple;

/**
 * Base class for everything that sits in the pond.
 * <p>
 * Every element is a circle: it has a centre position and a size, which is its
 * diameter. Both are measured in play-area widths (GDD section 2.1), so they do not
 * depend on the size of the window.
 * <p>
 * Reacting to a ripple uses the Template method pattern: {@link #reactTo} is the fixed
 * part, written once here and declared {@code final}, and subclasses only fill in
 * {@link #onRipple}. A floating object is pushed, a turtle wakes up, and every other
 * element keeps the default, which does nothing.
 */
public abstract class AbstractElement implements Element {

    private Vec2 position;
    private final double size;

    /**
     * Creates an element.
     *
     * @param position the centre of the element, in play-area widths
     * @param size     the diameter of the element, in play-area widths; greater than 0
     * @throws NullPointerException     if {@code position} is {@code null}
     * @throws IllegalArgumentException if {@code size} is not greater than 0
     */
    protected AbstractElement(Vec2 position, double size) {
        this.position = Objects.requireNonNull(position, "position");
        // Written as "not greater than 0" so that NaN is rejected too.
        if (!(size > 0)) {
            throw new IllegalArgumentException("size must be greater than 0: " + size);
        }
        this.size = size;
    }

    /**
     * Returns the centre of this element.
     *
     * @return the centre, in play-area widths
     */
    @Override
    public Vec2 getPosition() {
        return position;
    }

    /**
     * Returns the diameter of this element.
     *
     * @return the diameter, in play-area widths
     */
    @Override
    public double getSize() {
        return size;
    }

    /**
     * Returns the radius of this element, half its diameter.
     *
     * @return the radius, in play-area widths
     */
    @Override
    public double getRadius() {
        return size / 2;
    }

    /**
     * Returns the distance between the centre of this element and the centre of
     * another.
     *
     * @param other the other element
     * @return the distance between the two centres, in play-area widths
     */
    @Override
    public double distanceTo(Element other) {
        return position.distanceTo(other.getPosition());
    }

    /**
     * Returns {@code true} when this element and another overlap or just touch, that is
     * when their centres are no further apart than the sum of their radii.
     *
     * @param other the other element
     * @return whether the two circles touch
     */
    @Override
    public boolean touches(Element other) {
        return distanceTo(other) <= getRadius() + other.getRadius();
    }

    /**
     * Lets this element react to a ripple. This is the fixed part of the Template
     * method: it asks the ripple whether its ring passes this element this turn and,
     * only if it does, calls {@link #onRipple}. Subclasses cannot change this order.
     *
     * @param ripple the ripple to react to
     * @param pond   the pond the element is in
     */
    @Override
    public final void reactTo(Ripple ripple, Pond pond) {
        if (ripple.passes(this, pond)) {
            onRipple(ripple, pond);
        }
    }

    /**
     * What this element does when a ripple's ring passes it. The default does nothing;
     * subclasses that move or change when a ripple reaches them override it.
     *
     * @param ripple the ripple whose ring passed this element
     * @param pond   the pond the element is in
     */
    protected void onRipple(Ripple ripple, Pond pond) {
        // Most elements ignore ripples.
    }

    /**
     * Moves the centre of this element to a new position. Only subclasses can move an
     * element, so every move goes through the rules of the subclass.
     *
     * @param target the new centre, in play-area widths
     * @throws NullPointerException if {@code target} is {@code null}
     */
    protected void moveTo(Vec2 target) {
        this.position = Objects.requireNonNull(target, "target");
    }
}
