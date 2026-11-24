package ripplepond.model.movement;

import ripplepond.model.Balance;
import ripplepond.model.element.Frog;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.Pond;

/**
 * Hopping: a jump over water from one surface to another (GDD 4.2). Always available;
 * its range grows with the frog's rank.
 * <p>
 * The frog must take off from a surface (a bank or a floating object, not the water)
 * and land on one. A hop goes over reed clumps, but cannot land inside one.
 */
public final class Hop extends AbstractMoveMode {

    @Override
    public double energyPerUnit() {
        return Balance.HOP_ENERGY_PER_UNIT;
    }

    @Override
    public double maxDistance(Frog frog) {
        return frog.getRank().hopRange();
    }

    @Override
    protected boolean allows(Frog frog, Vec2 target, Pond pond) {
        if (!onSurface(frog, pond)) {
            return false;
        }
        boolean landsOnSurface = onBank(target, pond) || pond.surfaceAt(target).isPresent();
        // A segment from the target to itself is blocked only if the target is inside a reed.
        boolean landsInReed = pond.reedBetween(target, target);
        return landsOnSurface && !landsInReed;
    }

    @Override
    public String toString() {
        return "Hop";
    }
}
