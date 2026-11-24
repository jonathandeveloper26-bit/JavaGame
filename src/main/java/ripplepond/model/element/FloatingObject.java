package ripplepond.model.element;

import java.util.Objects;
import java.util.Optional;

import ripplepond.model.event.EventType;
import ripplepond.model.event.GameEvent;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.Level;
import ripplepond.model.level.Pond;

/**
 * Something that floats on the water and that the frog can stand on: a thrown object or
 * a lily pad (GDD section 2.2).
 * <p>
 * When ripples reach it, it is pushed away from their sources (GDD 3.1) and publishes
 * {@code OBJECT_PUSHED}. A floating object can carry one frog, its rider. When the
 * object is pushed, the rider moves with it by the same amount. This is the riding rule
 * (GDD section 3, step 3).
 * <p>
 * The link between the frog and the object it stands on is kept on both sides:
 * {@link Frog#standingOn()} and {@link #getRider()} always agree. Only
 * {@code Frog.standOn} changes it, so the two sides cannot drift apart.
 * <p>
 * A push is blocked by reed clumps, turtles, other floating objects and the banks.
 */
public abstract class FloatingObject extends AbstractElement {

    private Frog rider;

    /**
     * Creates a floating object.
     *
     * @param position the centre of the object, in play-area widths
     * @param size     the diameter of the object, in play-area widths; greater than 0
     * @throws NullPointerException     if {@code position} is {@code null}
     * @throws IllegalArgumentException if {@code size} is not greater than 0
     */
    protected FloatingObject(Vec2 position, double size) {
        super(position, size);
    }

    /**
     * Returns {@code true} when a point lies on this object, edge included. A frog can
     * stand on the object when its centre is covered.
     *
     * @param point the point to test, in play-area widths
     * @return whether the point is on the object
     * @throws NullPointerException if {@code point} is {@code null}
     */
    public boolean covers(Vec2 point) {
        Objects.requireNonNull(point, "point");
        return getPosition().distanceTo(point) <= getRadius();
    }

    /**
     * Returns the frog standing on this object, if any.
     *
     * @return the rider, or an empty {@code Optional}
     */
    public Optional<Frog> getRider() {
        return Optional.ofNullable(rider);
    }

    /**
     * Returns {@code true}: a floating object takes up its space, so other floating
     * objects and turtles cannot be pushed into it.
     *
     * @return {@code true}
     */
    @Override
    public boolean isSolid() {
        return true;
    }

    /**
     * Pushes this object, and its rider with it.
     * <p>
     * The object moves {@code distance} along {@code direction}. Only the direction of
     * {@code direction} counts, not its length. If there is a rider, it moves by exactly
     * the same amount, so it stays at the same spot on the object.
     * <p>
     * The push is blocked, and nothing moves, if the object would end up overlapping a
     * reed clump, a turtle or another floating object, reaching onto a bank, or leaving
     * the play area (GDD 3.1). Ending up just touching something is allowed.
     *
     * @param direction the direction to push in; its length does not matter
     * @param distance  how far to push, in play-area widths; 0 or more
     * @param pond      the pond the object is in
     * @return {@code true} if the object moved; {@code false} for a distance of 0, a
     *         direction of zero length, or a blocked push
     * @throws NullPointerException     if {@code direction} or {@code pond} is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code distance} is negative or not a number
     */
    public boolean push(Vec2 direction, double distance, Pond pond) {
        Objects.requireNonNull(direction, "direction");
        Objects.requireNonNull(pond, "pond");
        if (!(distance >= 0)) {
            throw new IllegalArgumentException("distance must be 0 or more: " + distance);
        }
        if (distance == 0 || direction.length() == 0) {
            return false;
        }
        Vec2 offset = direction.normalised().scale(distance);
        Vec2 target = getPosition().plus(offset);
        if (!pond.isClearFor(this, target)) {
            return false;
        }
        moveTo(target);
        if (rider != null) {
            rider.moveTo(rider.getPosition().plus(offset));
        }
        return true;
    }

    /**
     * Moves the object by the ripples' combined push and, if it moved, publishes
     * {@code OBJECT_PUSHED} with the distance moved as its amount. A zero push, from
     * ripples that cancel out, moves nothing and publishes nothing.
     *
     * @param push  the combined push
     * @param level the level being played
     */
    @Override
    protected void onRipple(Vec2 push, Level level) {
        double distance = push.length();
        if (push(push, distance, level.getPond())) {
            level.publish(new GameEvent(EventType.OBJECT_PUSHED, this, distance));
        }
    }

    /**
     * Records which frog stands on this object. Only {@code Frog.standOn} calls this,
     * so that both sides of the link change together.
     *
     * @param frog the new rider, or {@code null} when the frog steps off
     */
    void setRider(Frog frog) {
        this.rider = frog;
    }
}
