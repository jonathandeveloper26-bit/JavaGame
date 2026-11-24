package ripplepond.model.level;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import ripplepond.model.element.ThrowableKind;
import ripplepond.model.element.ThrownObject;
import ripplepond.model.geometry.Vec2;

/**
 * The objects the player can still throw in this level (GDD section 4.4).
 * <p>
 * The bag only counts how many objects of each kind it holds; an object is created when
 * it is thrown. A level starts with the bag from its level file, and the frog adds to it
 * by touching throwable icons. Counts never go below 0: taking a kind the bag does not
 * hold is refused.
 * <p>
 * An empty bag does not lose the level (GDD section 3.2); the frog may still walk or hop
 * to the far bank.
 */
public class ThrowBag {

    private final Map<ThrowableKind, Integer> counts = new EnumMap<>(ThrowableKind.class);

    /**
     * Creates an empty bag.
     */
    public ThrowBag() {
        // Kinds that are not in the map have a count of 0.
    }

    /**
     * Puts one object of a kind in the bag.
     *
     * @param kind the kind of object to add
     * @throws NullPointerException if {@code kind} is {@code null}
     */
    public void add(ThrowableKind kind) {
        Objects.requireNonNull(kind, "kind");
        counts.merge(kind, 1, Integer::sum);
    }

    /**
     * Returns how many objects of a kind are in the bag.
     *
     * @param kind the kind to count
     * @return the number of objects of that kind, 0 or more
     * @throws NullPointerException if {@code kind} is {@code null}
     */
    public int count(ThrowableKind kind) {
        Objects.requireNonNull(kind, "kind");
        return counts.getOrDefault(kind, 0);
    }

    /**
     * Returns {@code true} if the bag holds at least one object of a kind.
     *
     * @param kind the kind to look for
     * @return whether an object of that kind can be thrown
     * @throws NullPointerException if {@code kind} is {@code null}
     */
    public boolean has(ThrowableKind kind) {
        return count(kind) > 0;
    }

    /**
     * Returns {@code true} if the bag holds nothing at all.
     *
     * @return whether the bag is empty
     */
    public boolean isEmpty() {
        return counts.isEmpty();
    }

    /**
     * Takes one object of a kind out of the bag and returns it, landed at the point where
     * it was thrown. The new object has the usual size for its kind.
     *
     * @param kind   the kind of object to throw
     * @param target where the object lands, in play-area widths
     * @return the thrown object, centred on {@code target}
     * @throws NullPointerException  if {@code kind} or {@code target} is {@code null}
     * @throws IllegalStateException if the bag holds no object of that kind; the bag is
     *                               then unchanged
     */
    public ThrownObject take(ThrowableKind kind, Vec2 target) {
        Objects.requireNonNull(target, "target");
        if (!has(kind)) {
            throw new IllegalStateException("no " + kind + " left in the bag");
        }
        // Remove the kind when its count reaches 0, so isEmpty stays a simple check.
        counts.computeIfPresent(kind, (k, n) -> n == 1 ? null : n - 1);
        return new ThrownObject(target, kind);
    }
}
