package ripplepond.model.level;

import ripplepond.model.geometry.Vec2;

/**
 * A full-width horizontal strip of the pond: a bank or the water.
 * <p>
 * A strip includes its top edge and excludes its bottom edge, so adjacent strips never both
 * claim the same point. The bottom edge of the whole play area is handled by {@link Pond}.
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
     * @param bottom the lower edge, in play-area units; must be greater than {@code top}
     * @throws NullPointerException     if {@code type} is {@code null}
     * @throws IllegalArgumentException if {@code bottom} is not greater than {@code top}
     */
    public Area(AreaType type, double top, double bottom) {
        this.type = java.util.Objects.requireNonNull(type, "type");
        if (!(bottom > top)) {
            throw new IllegalArgumentException("bottom must be greater than top: " + top + ", " + bottom);
        }
        this.top = top;
        this.bottom = bottom;
    }

    /** Returns the kind of this area. */
    public AreaType getType() {
        return type;
    }

    /**
     * Returns {@code true} if the point lies in this strip: at or below the top edge and
     * above the bottom edge.
     *
     * @param point the point to test
     * @return whether the point is inside this strip
     * @throws NullPointerException if {@code point} is {@code null}
     */
    public boolean contains(Vec2 point) {
        java.util.Objects.requireNonNull(point, "point");
        return point.getY() >= top && point.getY() < bottom;
    }
}