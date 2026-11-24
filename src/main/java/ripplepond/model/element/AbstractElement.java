package ripplepond.model.element;

import java.util.Objects;

import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.Level;

/**
 * Base class for everything that sits in the pond.
 * <p>
 * Every element is a circle: it has a centre position and a size, which is its
 * diameter. Both are measured in play-area widths (GDD section 2.1), so they do not
 * depend on the size of the window.
 * <p>
 * Reacting to ripples uses the Template method pattern: {@link #reactTo} is the fixed
 * part, written once here and declared {@code final}, and subclasses only fill in
 * {@link #onRipple}. A floating object is pushed, a turtle wakes up, and every other
 * element keeps the default, which does nothing. Which ripples passed and how their
 * pushes add up is decided by the turn engine, so every element reacts to the same,
 * combined push.
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
     * Lets this element react to the ripples that reached it this turn. This is the
     * fixed part of the Template method: it checks its arguments, then hands the push to
     * {@link #onRipple}. Subclasses cannot change it, only what {@code onRipple} does.
     *
     * @param push  the combined push of every ripple that passed this element this turn
     * @param level the level being played
     * @throws NullPointerException if {@code push} or {@code level} is {@code null}
     */
    @Override
    public final void reactTo(Vec2 push, Level level) {
        Objects.requireNonNull(push, "push");
        Objects.requireNonNull(level, "level");
        onRipple(push, level);
    }

    /**
     * What this element does when ripples reach it. The default does nothing; subclasses
     * that move or change when a ripple reaches them override it.
     *
     * @param push  the combined push, at most one push distance long; can be zero
     * @param level the level being played, to see the pond and publish events
     */
    protected void onRipple(Vec2 push, Level level) {
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
