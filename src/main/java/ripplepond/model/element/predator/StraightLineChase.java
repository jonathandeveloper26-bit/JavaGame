package ripplepond.model.element.predator;

import java.util.Objects;

import ripplepond.model.geometry.Vec2;

/**
 * Moves in a straight line towards the target, stopping at the target if it is closer than
 * one step (GDD 2.4).
 */
public final class StraightLineChase implements ChaseStrategy {

    @Override
    public Vec2 nextPosition(Vec2 from, Vec2 target, double maxStep) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(target, "target");
        if (!(maxStep >= 0)) {
            throw new IllegalArgumentException("maxStep must be 0 or more: " + maxStep);
        }
        Vec2 offset = target.minus(from);
        if (offset.length() <= maxStep) {
            return target;
        }
        return from.plus(offset.normalised().scale(maxStep));
    }
}
