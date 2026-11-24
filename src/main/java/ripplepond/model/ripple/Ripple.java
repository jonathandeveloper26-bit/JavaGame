package ripplepond.model.ripple;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import ripplepond.model.Balance;
import ripplepond.model.element.Element;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.Pond;

/**
 * A ring of disturbance that spreads outwards from a point on the water.
 *
 * <p>A ripple remembers the radius it had in the previous turn and the radius it has now,
 * so it can tell the one turn in which its ring first reaches an element's nearest edge.
 * It pushes each element at most once, never pushes the object that started it, and is
 * blocked by reed clumps (GDD section 3.1). It fades after {@link Balance#RIPPLE_LIFETIME_TURNS}
 * spread steps.
 *
 * <p>How the ring grows is decided by a {@link RippleSpread} strategy (Strategy pattern).
 */
public final class Ripple {

    private final Vec2 source;
    private final Element origin;
    private final RippleSpread spread;
    private final Set<Element> alreadyPassed = new HashSet<>();

    private double radius;
    private double previousRadius;
    private int turnsLeft;

    /**
     * Starts a ripple at a point, with radius zero and full strength.
     *
     * @param source the point where the ripple starts, normally where the object landed
     * @param origin the element that started the ripple; it is never pushed by it
     * @param spread the strategy that decides how the ring grows and when it reaches elements
     */
    public Ripple(Vec2 source, Element origin, RippleSpread spread) {
        this.source = Objects.requireNonNull(source, "source");
        this.origin = Objects.requireNonNull(origin, "origin");
        this.spread = Objects.requireNonNull(spread, "spread");
        this.radius = 0.0;
        this.previousRadius = 0.0;
        this.turnsLeft = Balance.RIPPLE_LIFETIME_TURNS;
    }

    /**
     * Performs one spread step: the ring grows and one turn of strength is used up.
     * Does nothing once the ripple has faded.
     *
     * <p>The {@code pond} parameter is part of the UML contract for this method but is not
     * needed yet; see the concordance notes. It is kept so the call sites in the turn
     * engine do not change.
     *
     * @param pond the pond the ripple travels across (currently unused)
     */
    public void advance(Pond pond) {
        if (isFaded()) {
            return;
        }
        previousRadius = radius;
        radius = spread.nextRadius(this);
        turnsLeft--;
    }

    /**
     * Tells whether this ripple's ring first reaches the given element in the current turn.
     *
     * <p>Returns {@code true} only when all of the following hold: the element is not the
     * origin, it has not been passed before, the ring crosses its nearest edge during the
     * last spread step, and no reed clump lies on the line from the source to the element.
     * When it returns {@code true} it records the element, so it can never return
     * {@code true} for that element again. This side effect is what makes the push happen
     * once (GDD 3.1, UML view 3).
     *
     * @param element the element to test
     * @param pond the pond, used to find reed clumps that shadow the element
     * @return {@code true} if the element should be pushed by this ripple now
     */
    public boolean passes(Element element, Pond pond) {
        if (element == origin || alreadyPassed.contains(element)) {
            return false;
        }
        if (!spread.ringCrosses(this, element)) {
            return false;
        }
        if (pond.reedBetween(source, element.getPosition())) {
            return false;
        }
        alreadyPassed.add(element);
        return true;
    }

    /**
     * Gives the direction a push from this ripple takes an element: the unit vector from
     * the source towards the element's centre.
     *
     * @param element the element being pushed
     * @return the unit direction, or the zero vector if the element's centre is at the source
     */
    public Vec2 pushDirection(Element element) {
        Vec2 offset = element.getPosition().minus(source);
        if (offset.length() == 0.0) {
            return offset;
        }
        return offset.normalised();
    }

    /**
     * Tells whether the ripple has used up its strength and should be removed.
     *
     * @return {@code true} once no spread steps are left
     */
    public boolean isFaded() {
        return turnsLeft <= 0;
    }

    /** @return the point where the ripple started */
    public Vec2 getSource() {
    Vec2 getSource() {
        return source;
    }

    /** @return the current radius, in play-area widths */
    public double getRadius() {
    double getRadius() {
        return radius;
    }

    /** @return the radius before the last spread step */
    public double getPreviousRadius() {
    double getPreviousRadius() {
        return previousRadius;
    }
}