package ripplepond.model.element.predator;

import ripplepond.model.Balance;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.AreaType;
import ripplepond.model.level.Pond;

/** A snake: lives on the banks and in reed clumps (GDD 2.4). */
public final class Snake extends Predator {

    public Snake(Vec2 position, double size) {
        super(position, size, new StraightLineChase());
    }

    @Override
    public int getDamage() {
        return Balance.SNAKE_DAMAGE;
    }

    @Override
    protected boolean canEnter(Pond pond, Vec2 centre) {
        return pond.areaAt(centre) != AreaType.WATER || pond.isInReed(centre);
    }
}
