package ripplepond.model.element;

import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.Pond;
import ripplepond.model.ripple.Ripple;

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

    /** Responds to a ripple that reaches this element. */
    void reactTo(Ripple ripple, Pond pond);
}
