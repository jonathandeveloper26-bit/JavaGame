package ripplepond.model.movement;

import ripplepond.model.Balance;
import ripplepond.model.element.Frog;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.AreaType;
import ripplepond.model.level.Pond;

/**
 * Swimming: through open water (GDD 4.2). Unlocked by rank; the most expensive move.
 * <p>
 * The frog must have a rank that can swim. The move must be in the water at one end at
 * least: the frog can dive in from a bank or an object, swim on, or climb out onto a
 * bank or an object. A move from bank to bank is a walk, not a swim. A reed clump on the
 * way blocks it.
 */
public final class Swim extends AbstractMoveMode {

    @Override
    public double energyPerUnit() {
        return Balance.SWIM_ENERGY_PER_UNIT;
    }

    @Override
    public double maxDistance(Frog frog) {
        return Balance.SWIM_MAX_DISTANCE;
    }

    @Override
    protected boolean allows(Frog frog, Vec2 target, Pond pond) {
        if (!frog.getRank().canSwim()) {
            return false;
        }
        boolean throughWater = pond.areaAt(frog.getPosition()) == AreaType.WATER
                || pond.areaAt(target) == AreaType.WATER;
        return throughWater && !pond.reedBetween(frog.getPosition(), target);
    }

    @Override
    public String toString() {
        return "Swim";
    }
}
