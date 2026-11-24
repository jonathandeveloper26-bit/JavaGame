package ripplepond.model.element;

import ripplepond.model.event.EventType;
import ripplepond.model.event.GameEvent;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.Level;
import ripplepond.model.level.Pond;

/**
 * A turtle: it sleeps on the bottom of the river and blocks the space it occupies. It is
 * not an enemy (GDD section 2.2).
 * <p>
 * A turtle starts asleep. When a ripple's ring passes it, it wakes and moves away from
 * the ripple's source, by the same push a floating object would get. The same things
 * block it: reed clumps, floating objects, other turtles and the banks. It moves once
 * for each turn a ripple reaches it [proposed; GDD 10.1 leaves open whether it should
 * keep moving].
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

    /**
     * Wakes the turtle and moves it by the push, unless something is in the way.
     * <p>
     * The first time the turtle wakes it publishes {@code TURTLE_WOKE}, with the distance
     * it moved as the amount. Being woken again does not publish it again. A push that is
     * blocked, or zero because ripples cancelled out, still wakes the turtle but does not
     * move it.
     *
     * @param push  the combined push of the ripples that reached the turtle
     * @param level the level being played
     */
    @Override
    protected void onRipple(Vec2 push, Level level) {
        boolean firstWake = !awake;
        awake = true;
        double moved = 0;
        Pond pond = level.getPond();
        Vec2 target = getPosition().plus(push);
        if (push.length() > 0 && pond.isClearFor(this, target)) {
            moveTo(target);
            moved = push.length();
        }
        if (firstWake) {
            level.publish(new GameEvent(EventType.TURTLE_WOKE, this, moved));
        }
    }
}
