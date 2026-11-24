package ripplepond.model.movement;

import java.util.Optional;

import ripplepond.model.Balance;
import ripplepond.model.element.FloatingObject;
import ripplepond.model.element.Frog;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.Pond;

/**
 * Walking: across a bank, or within the object the frog is standing on (GDD 4.2). The
 * cheapest move, always available, but short.
 * <p>
 * A walk stays on the same surface: from a bank to a point on the same bank, or from an
 * object to a point on the same object. A reed clump in the way blocks it. Getting from
 * one surface to another needs a hop.
 */
public final class Walk extends AbstractMoveMode {

    @Override
    public double energyPerUnit() {
        return Balance.WALK_ENERGY_PER_UNIT;
    }

    @Override
    public double maxDistance(Frog frog) {
        return Balance.WALK_MAX_DISTANCE;
    }

    @Override
    protected boolean allows(Frog frog, Vec2 target, Pond pond) {
        if (pond.reedBetween(frog.getPosition(), target)) {
            return false;
        }
        Optional<FloatingObject> object = frog.standingOn();
        if (object.isPresent()) {
            return object.get().covers(target);
        }
        return onBank(frog.getPosition(), pond)
                && pond.areaAt(target) == pond.areaAt(frog.getPosition());
    }

    @Override
    public String toString() {
        return "Walk";
    }
}
