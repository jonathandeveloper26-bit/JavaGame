package ripplepond.model.level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ripplepond.model.element.Element;
import ripplepond.model.element.Frog;
import ripplepond.model.element.FloatingObject;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.ripple.Ripple;

/**
 * The play area: its strips, the elements in it, the active ripples and the frog.
 */
public class Pond {

    private final double width;
    private final double height;
    private final Frog frog;
    private final List<Area> areas = new ArrayList<>();
    private final List<Element> elements = new ArrayList<>();
    private final List<Ripple> ripples = new ArrayList<>();

    /**
     * Creates an empty pond.
     *
     * @param width  the width of the play area
     * @param height the height of the play area
     * @param frog   the frog that the player controls
     */
    public Pond(double width, double height, Frog frog) {
        this.width = width;
        this.height = height;
        this.frog = frog;
    }

    /** Returns the width of the play area. */
    public double getWidth() {
        return width;
    }

    /** Returns the height of the play area. */
    public double getHeight() {
        return height;
    }

    /** Returns the frog. */
    public Frog getFrog() {
        return frog;
    }

    /**
     * Returns the kind of area at a point. Implemented in S3.
     *
     * @param point the point to look up
     * @return the area type at that point
     * @throws UnsupportedOperationException until S3 is merged
     */
    public AreaType areaAt(Vec2 point) {
        throw new UnsupportedOperationException("Implemented in S3");
    }

    /**
     * Returns {@code true} if an element of the given size can be placed at the point
     * without overlapping anything. Implemented in S3.
     *
     * @param point the centre of the proposed element
     * @param size  the diameter of the proposed element
     * @return whether the spot is free
     * @throws UnsupportedOperationException until S3 is merged
     */
    public boolean isFree(Vec2 point, double size) {
        throw new UnsupportedOperationException("Implemented in S3");
    }

    /**
     * Returns the floating object the point lies on, if any. Implemented in S3.
     *
     * @param point the point to test
     * @return the floating object under the point, or empty if the point is on open
     *         water or a bank
     * @throws UnsupportedOperationException until S3 is merged
     */
    public Optional<FloatingObject> surfaceAt(Vec2 point) {
        throw new UnsupportedOperationException("Implemented in S3");
    }

    /**
     * Returns {@code true} if a reed clump lies on the straight line between two points.
     * Implemented in S3.
     *
     * @param from the first point
     * @param to   the second point
     * @return whether a reed blocks the line
     * @throws UnsupportedOperationException until S3 is merged
     */
    public boolean reedBetween(Vec2 from, Vec2 to) {
        throw new UnsupportedOperationException("Implemented in S3");
    }

    /**
     * Returns the elements sorted top to bottom, then left to right, so every turn
     * processes them in the same order. Implemented in S3.
     *
     * @return a new list, leaving the stored order unchanged
     * @throws UnsupportedOperationException until S3 is merged
     */
    public List<Element> elementsInOrder() {
        throw new UnsupportedOperationException("Implemented in S3");
    }

    /**
     * Adds an element to the pond.
     *
     * @param element the element to add
     */
    public void add(Element element) {
        elements.add(element);
    }

    /**
     * Removes an element from the pond, if it is present.
     *
     * @param element the element to remove
     */
    public void remove(Element element) {
        elements.remove(element);
    }
}