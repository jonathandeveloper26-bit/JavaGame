package ripplepond.model.ripple;

import ripplepond.model.element.Element;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.Pond;

/**
 * A minimal element for ripple tests. It has a position and a size and ignores ripples.
 * Using it keeps the ripple tests independent of the concrete element classes that other
 * team members write.
 */
final class StubElement implements Element {

    private final Vec2 position;
    private final double size;

    StubElement(Vec2 position, double size) {
        this.position = position;
        this.size = size;
    }

    @Override
    public Vec2 getPosition() {
        return position;
    }

    @Override
    public double getSize() {
        return size;
    }

    @Override
    public double getRadius() {
        return size / 2.0;
    }

    @Override
    public boolean touches(Element other) {
        return distanceTo(other) <= getRadius() + other.getRadius();
    }

    @Override
    public double distanceTo(Element other) {
        return position.distanceTo(other.getPosition());
    }

    @Override
    public void reactTo(Ripple ripple, Pond pond) {
        // A stub does nothing when a ripple passes it.
    }
}