package ripplepond.model.element.predator;

import ripplepond.model.geometry.Vec2;

/**
 * How a predator moves towards a point (Strategy pattern, GDD 2.4). A new chase rule is a
 * new implementation, and {@code Predator} does not change.
 */
public interface ChaseStrategy {

    /**
     * Returns the position a predator reaches when it moves up to {@code maxStep} towards
     * {@code target}. It never overshoots the target.
     *
     * @param from    where the predator is now
     * @param target  where it wants to go
     * @param maxStep the furthest it may move, in play-area widths; 0 or more
     * @return the new position
     */
    Vec2 nextPosition(Vec2 from, Vec2 target, double maxStep);
}
