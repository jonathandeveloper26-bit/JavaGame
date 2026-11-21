package ripplepond.model.ripple;

import ripplepond.model.element.Element;
import ripplepond.model.level.Pond;

/**
 * A ring that spreads from where an object landed and pushes the elements it reaches.
 * Implemented in S4.
 */
public class Ripple {

    /**
     * Returns {@code true} if this ripple's ring first reaches the element this turn.
     * Implemented in S4.
     *
     * @param element the element to test
     * @param pond    the pond, used to check for reed clumps
     * @return whether the ring reaches the element for the first time this turn
     * @throws UnsupportedOperationException until S4 is merged
     */
    public boolean passes(Element element, Pond pond) {
        throw new UnsupportedOperationException("Implemented in S4");
    }
}
