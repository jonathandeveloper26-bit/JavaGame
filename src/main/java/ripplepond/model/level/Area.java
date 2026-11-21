package ripplepond.model.level;

import ripplepond.model.geometry.Vec2;

/**
 * A full-width horizontal strip of the pond: a bank or the water.
 */
public class Area {

    private final AreaType type;
    private final double top;
    private final double bottom;

    /**
     * Creates a strip between two heights.
     *
     * @param type   which kind of area this is
     * @param top    the upper edge, in play-area units
     * @param bottom the lower edge, in play-area units, greater than {@code top}
     */
    public Area(AreaType type, double top, double bottom) {
        this.type = type;
        this.top = top;
        this.bottom = bottom;
    }

    /** Returns the kind of this area. */
    public AreaType getType() {
        return type;
    }

    /**
     * Returns {@code true} if the point lies in this strip. The top edge is included
     * and the bottom edge is not, so adjacent strips never both claim a point.
     *
     * @param point the point to test
     * @return whether the point is inside this strip
     */
    public boolean contains(Vec2 point) {
        return point.getY() >= top && point.getY() < bottom;
    }
}
