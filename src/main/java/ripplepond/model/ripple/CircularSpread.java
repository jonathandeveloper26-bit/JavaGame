package ripplepond.model.ripple;

import ripplepond.model.Balance;
import ripplepond.model.element.Element;

/**
 * The circular spreading rule: the ring grows by a fixed distance each turn.
 *
 * <p>An element's nearest edge is at distance {@code d - r} from the source, where {@code d}
 * is the distance to its centre and {@code r} its radius. The ring crosses that edge in the
 * step where the previous radius is still short of it and the new radius has reached it.
 */
public final class CircularSpread implements RippleSpread {

    /**
     * Grows the ring by {@link Balance#RIPPLE_GROWTH_PER_TURN} each step.
     *
     * @param ripple the ripple being spread
     * @return the current radius plus the growth amount
     */
    @Override
    public double nextRadius(Ripple ripple) {
        return ripple.getRadius() + Balance.RIPPLE_GROWTH_PER_TURN;
    }

    /**
     * Tests whether the ring first reaches the element's nearest edge in this step.
     * An element whose edge lies at or inside the source is never reached.
     *
     * @param ripple the ripple, with its previous and current radius
     * @param element the element to test
     * @return {@code true} only in the step where the ring crosses the nearest edge
     */
    @Override
    public boolean ringCrosses(Ripple ripple, Element element) {
        double nearestEdge = ripple.getSource().distanceTo(element.getPosition()) - element.getRadius();
        return ripple.getPreviousRadius() < nearestEdge && nearestEdge <= ripple.getRadius();
    }
}