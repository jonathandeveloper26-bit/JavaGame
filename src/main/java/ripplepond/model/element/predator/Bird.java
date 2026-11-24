package ripplepond.model.element.predator;

import ripplepond.model.Balance;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.Pond;

/** A bird: flies over any area (GDD 2.4). */
public final class Bird extends Predator {

    public Bird(Vec2 position, double size) {
        super(position, size, new StraightLineChase());
    }

    @Override
    public int getDamage() {
        return Balance.BIRD_DAMAGE;
    }

    @Override
    protected boolean canEnter(Pond pond, Vec2 centre) {
        return true;
    }
}