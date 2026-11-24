package ripplepond.model.element;

import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.Level;

/**
 * Anything that sits in the pond and can be affected by ripples.
 */
public interface Element {

    /** The centre of the element, in play-area units. */
    Vec2 getPosition();

    /** The diameter of the element, in play-area units. */
    double getSize();

    /** Half the diameter of the element. */
    double getRadius();

    /** True when this element and {@code other} overlap. */
    boolean touches(Element other);

    /** The distance between the centres of this element and {@code other}. */
    double distanceTo(Element other);

    /**
     * Returns {@code true} if this element takes up its space, so that a floating object
     * or a turtle cannot be pushed into it (GDD 2.2, 3.1). Reed clumps, turtles and
     * floating objects are solid; by default an element is not, so pickups such as food
     * and throwable icons never block anything.
     *
     * @return whether this element blocks things that are pushed
     */
    default boolean isSolid() {
        return false;
    }

    /**
     * Responds to the ripples that reached this element this turn. The turn engine calls
     * it once per turn for each element that at least one ripple's ring passed, with the
     * combined push of all those ripples (GDD 3.1).
     *
     * @param push  where the ripples push the element: the direction away from their
     *              sources, at most one push distance long; can be zero if pushes cancel
     * @param level the level being played, so the element can see the pond and publish
     *              what happened to it
     */
    void reactTo(Vec2 push, Level level);
    default boolean blocksLine(Vec2 from, Vec2 to) {
        return false;
    }
}
