package ripplepond.model.level;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import ripplepond.model.element.Element;

/**
 * The fixed order in which elements are processed during a turn (GDD section 3, step 3).
 * <p>
 * Elements are ordered by the centre of each: top to bottom, then left to right. The same
 * pond always gives the same order, which keeps every turn deterministic (GDD pillar 4).
 * <p>
 * This class holds no state and changes nothing in the pond. It only returns new lists.
 */
public final class ElementOrder {

    /** Top to bottom (smaller y first), then left to right (smaller x first). */
    public static final Comparator<Element> TOP_THEN_LEFT =
            Comparator.comparingDouble((Element e) -> e.getPosition().getY())
                    .thenComparingDouble(e -> e.getPosition().getX());

    private ElementOrder() {
    }

    /**
     * Returns a new list with the given elements in turn order.
     * <p>
     * {@link List#sort} is stable, so elements with exactly the same centre keep the order
     * in which they were given. The input collection is not changed.
     *
     * @param elements the elements to order
     * @return a new list in turn order
     * @throws NullPointerException if {@code elements} is {@code null}
     */
    public static List<Element> sorted(Collection<? extends Element> elements) {
        List<Element> copy = new ArrayList<>(elements);
        copy.sort(TOP_THEN_LEFT);
        return copy;
    }
}
