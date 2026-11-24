package ripplepond.model.element;

import java.util.Objects;

import ripplepond.model.geometry.Vec2;

/**
 * A reed clump: a solid circle that never moves and ignores ripples.
 * <p>
 * Reed clumps block floating objects and the frog, and they cast a "shadow" for
 * ripples: a ripple does not reach an element if a reed clump lies on the straight line
 * between the ripple's source and the element's centre (GDD section 3.1). The pond
 * answers that question by asking each clump {@link #liesBetween}.
 */
public final class Reed extends AbstractElement {

    /**
     * Creates a reed clump.
     *
     * @param position the centre of the clump, in play-area widths
     * @param size     the diameter of the clump, in play-area widths; greater than 0
     * @throws NullPointerException     if {@code position} is {@code null}
     * @throws IllegalArgumentException if {@code size} is not greater than 0
     */
    public Reed(Vec2 position, double size) {
        super(position, size);
    }

    /**
     * Returns {@code true}: a reed clump blocks floating objects and turtles.
     *
     * @return {@code true}
     */
    @Override
    public boolean isSolid() {
        return true;
    }

    /**
     * Returns {@code true} when the straight segment from {@code from} to {@code to}
     * passes through this clump.
     * <p>
     * It finds the point of the segment closest to the clump's centre and checks
     * whether that point is inside the clump. A segment that only grazes the edge, at
     * exactly one radius from the centre, is not blocked [proposed].
     *
     * @param from one end of the segment, in play-area widths
     * @param to   the other end of the segment, in play-area widths
     * @return whether the segment passes through the clump
     * @throws NullPointerException if {@code from} or {@code to} is {@code null}
     */
    public boolean liesBetween(Vec2 from, Vec2 to) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        Vec2 centre = getPosition();
        return closestPointOnSegment(from, to, centre).distanceTo(centre) < getRadius();
    }

    /**
     * Returns the point of the segment from {@code a} to {@code b} that is closest to
     * {@code point}.
     * <p>
     * The segment is every point {@code a + t * (b - a)} with {@code t} between 0 and 1.
     * The closest point on the endless line through {@code a} and {@code b} has
     * {@code t = ((point - a) . (b - a)) / |b - a|^2}; clamping {@code t} to the range
     * 0 to 1 keeps the answer on the segment itself.
     *
     * @param a     one end of the segment
     * @param b     the other end of the segment
     * @param point the point to measure from
     * @return the closest point of the segment
     */
    private static Vec2 closestPointOnSegment(Vec2 a, Vec2 b, Vec2 point) {
        Vec2 segment = b.minus(a);
        double lengthSquared = dot(segment, segment);
        if (lengthSquared == 0) {
            // Both ends are the same point, so the segment is just that point.
            return a;
        }
        double t = dot(point.minus(a), segment) / lengthSquared;
        double clamped = Math.max(0, Math.min(1, t));
        return a.plus(segment.scale(clamped));
    }

    /**
     * Returns the dot product of two vectors.
     *
     * @param u the first vector
     * @param v the second vector
     * @return {@code u.x * v.x + u.y * v.y}
     */
    private static double dot(Vec2 u, Vec2 v) {
        return u.getX() * v.getX() + u.getY() * v.getY();
    }
}
