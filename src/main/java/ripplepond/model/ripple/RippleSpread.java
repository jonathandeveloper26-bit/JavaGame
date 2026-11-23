package ripplepond.model.ripple;

import ripplepond.model.element.Element;

/**
 * Strategy for how a ripple's ring grows and when it reaches an element.
 *
 * <p>Separating this from {@link Ripple} means a different spreading rule (for example one
 * that is not circular) is a new class, with no change to {@code Ripple} (GDD 8.2, open for
 * extension).
 */
public interface RippleSpread {

    /**
     * Works out the ring's radius after the next spread step.
     *
     * @param ripple the ripple being spread; its current radius is the starting point
     * @return the new radius, in play-area widths
     */
    double nextRadius(Ripple ripple);

    /**
     * Tells whether the ring moved across the element's nearest edge in the last spread step.
     * True only in the single step where the ring first reaches the edge.
     *
     * @param ripple the ripple, with its previous and current radius
     * @param element the element to test
     * @return {@code true} if the ring crossed the element's nearest edge in this step
     */
    boolean ringCrosses(Ripple ripple, Element element);
}
