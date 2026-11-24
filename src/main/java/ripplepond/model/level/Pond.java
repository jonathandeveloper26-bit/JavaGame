package ripplepond.model.level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import ripplepond.model.element.Element;
import ripplepond.model.element.FloatingObject;
import ripplepond.model.element.Frog;
import ripplepond.model.element.Reed;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.ripple.Ripple;
import ripplepond.model.Balance;

/**
 * The play area: its banks and water, the elements in it, the active ripples and the frog.
 * <p>
 * The pond is the one place that knows where everything is. Other classes ask it questions
 * ({@link #areaAt}, {@link #isFree}, {@link #reedBetween}, {@link #elementsInOrder}) instead
 * of keeping their own lists of positions (GDD section 8.2, single responsibility).
 * <p>
 * The play area runs from y = 0 at the top to y = height at the bottom. The far bank is the
 * strip at the top, the start bank the strip at the bottom, and the water lies between them.
 * A bank of depth 0 is left out, so the areas never include an empty strip.
 */
public class Pond {

    /** Width of the play area, from {@link Balance}. Fixed at 1 so the play area keeps its 3:2 shape. */
    public static final double WIDTH = Balance.PLAY_AREA_WIDTH;

    /** Height of the play area, from {@link Balance}. Two thirds of the width. */
    public static final double HEIGHT = Balance.PLAY_AREA_HEIGHT;

    private final double width;
    private final double height;
    private final double farBankDepth;
    private final double startBankDepth;
    private final Frog frog;
    private final List<Area> areas;
    private final List<Element> elements = new ArrayList<>();
    private final List<Ripple> ripples = new ArrayList<>();

    /**
     * Creates a pond with no banks. Kept so that existing callers still compile; use the
     * five-argument constructor for a real level.
     *
     * @param width  the width of the play area
     * @param height the height of the play area
     * @param frog   the frog that the player controls @throws NullPointerException if {@code frog} is {@code null}.
     */
    public Pond(double width, double height, Frog frog) {
        this(width, height, 0, 0, frog);
    }

    /**
     * Creates an empty pond with banks of the given depths.
     *
     * @param width          the width of the play area, greater than 0
     * @param height         the height of the play area, greater than 0
     * @param farBankDepth   the depth of the far bank at the top, 0 or more
     * @param startBankDepth the depth of the start bank at the bottom, 0 or more
     * @param frog           the frog that the player controls
     * @throws IllegalArgumentException if a size is not valid or the banks overlap
     */
    public Pond(double width, double height, double farBankDepth, double startBankDepth, Frog frog) {
        if (!(width > 0) || !(height > 0)) {
            throw new IllegalArgumentException("width and height must be greater than 0");
        }
        if (farBankDepth < 0 || startBankDepth < 0) {
            throw new IllegalArgumentException("bank depths must not be negative");
        }
        if (farBankDepth + startBankDepth > height) {
            throw new IllegalArgumentException("the two banks overlap");
        }
        this.width = width;
        this.height = height;
        this.farBankDepth = farBankDepth;
        this.startBankDepth = startBankDepth;
        this.frog = Objects.requireNonNull(frog, "frog");
        this.areas = buildAreas(height, farBankDepth, startBankDepth);
    }

    /**
     * Builds the strips from the top of the play area to the bottom, leaving out any strip
     * that would be empty.
     */
    private static List<Area> buildAreas(double height, double farBankDepth, double startBankDepth) {
        List<Area> built = new ArrayList<>();
        double waterTop = farBankDepth;
        double waterBottom = height - startBankDepth;
        if (farBankDepth > 0) {
            built.add(new Area(AreaType.FAR_BANK, 0, waterTop));
        }
        if (waterBottom > waterTop) {
            built.add(new Area(AreaType.WATER, waterTop, waterBottom));
        }
        if (startBankDepth > 0) {
            built.add(new Area(AreaType.START_BANK, waterBottom, height));
        }
        return Collections.unmodifiableList(built);
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
     * Returns the kind of area at a point: the far bank at the top, the start bank at the
     * bottom, or water between them. The bottom edge of the play area belongs to the last
     * strip.
     *
     * @param point the point to look up, inside the play area
     * @return the area type at that point
     * @throws NullPointerException     if {@code point} is {@code null}
     * @throws IllegalArgumentException if the point is outside the play area
     */
    public AreaType areaAt(Vec2 point) {
        Objects.requireNonNull(point, "point");
        if (point.getX() < 0 || point.getX() > width || point.getY() < 0 || point.getY() > height) {
            throw new IllegalArgumentException("point is outside the play area: " + point);
        }
        for (Area area : areas) {
            if (area.contains(point)) {
                return area.getType();
            }
        }
        // Only the bottom edge (y == height) reaches here: it belongs to the last strip.
        return areas.get(areas.size() - 1).getType();
    }

    /**
     * Returns {@code true} if an element of the given diameter can be placed with its centre
     * at {@code point}: the whole circle lies in the water and touches no element and not
     * the frog.
     * <p>
     * Touching uses the same rule as {@link Element#touches}, so a circle that only just
     * touches another is not free.
     *
     * @param point the proposed centre
     * @param size  the proposed diameter, greater than 0
     * @return whether the spot is free
     * @throws NullPointerException     if {@code point} is {@code null}
     * @throws IllegalArgumentException if {@code size} is not greater than 0
     */
    public boolean isFree(Vec2 point, double size) {
        Objects.requireNonNull(point, "point");
        if (!(size > 0)) {
            throw new IllegalArgumentException("size must be greater than 0: " + size);
        }
        double radius = size / 2;
        if (point.getX() - radius < 0 || point.getX() + radius > width) {
            return false;
        }
        if (point.getY() - radius < farBankDepth || point.getY() + radius > height - startBankDepth) {
            return false;
        }
        for (Element element : elements) {
            if (point.distanceTo(element.getPosition()) <= radius + element.getRadius()) {
                return false;
            }
        }
        // The frog is not one of the elements, so it is checked on its own. Some test
        // ponds have no frog.
        if (point.distanceTo(frog.getPosition()) <= radius + frog.getRadius()) {
            return false;
        }
        return true;
    }

    /**
     * Returns {@code true} if an element that is already in the pond could move its
     * centre to {@code centre}: the whole circle stays in the water and inside the play
     * area, and it does not overlap any solid element (reed clump, turtle, floating
     * object) other than itself. Pickups do not block. Just touching is allowed, so a
     * pushed object can come to rest against another.
     * <p>
     * Unlike {@link #isFree}, which places a new thrown object, this ignores the element
     * itself and anything that is not solid.
     *
     * @param mover  the element that would move
     * @param centre the centre it would move to
     * @return whether the move is clear
     * @throws NullPointerException if {@code mover} or {@code centre} is {@code null}
     */
    public boolean isClearFor(Element mover, Vec2 centre) {
        Objects.requireNonNull(mover, "mover");
        Objects.requireNonNull(centre, "centre");
        double radius = mover.getRadius();
        if (centre.getX() - radius < 0 || centre.getX() + radius > width) {
            return false;
        }
        if (centre.getY() - radius < farBankDepth || centre.getY() + radius > height - startBankDepth) {
            return false;
        }
        for (Element element : elements) {
            if (element != mover && element.isSolid()
                    && centre.distanceTo(element.getPosition()) < radius + element.getRadius()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the floating object the point lies on, if any. When objects overlap, the
     * one whose centre is nearest the point is returned.
     *
     * @param point the point to test
     * @return the floating object under the point, or empty on open water or a bank
     * @throws NullPointerException if {@code point} is {@code null}
     */
    public Optional<FloatingObject> surfaceAt(Vec2 point) {
        Objects.requireNonNull(point, "point");
        FloatingObject best = null;
        double bestDistance = Double.POSITIVE_INFINITY;
        for (Element element : elements) {
            if (element instanceof FloatingObject floating && floating.covers(point)) {
                double distance = point.distanceTo(floating.getPosition());
                // Strict "<" keeps the earlier object when two are equally near.
                if (distance < bestDistance) {
                    best = floating;
                    bestDistance = distance;
                }
            }
        }
        return Optional.ofNullable(best);
    }

    /**
     * Returns {@code true} if a reed clump lies on the straight line between two points.
     * Each element is asked whether it is a reed, and each reed answers for itself.
     *
     * @param from the first point
     * @param to   the second point
     * @return whether a reed blocks the line
     * @throws NullPointerException if {@code from} or {@code to} is {@code null}
     */
    public boolean reedBetween(Vec2 from, Vec2 to) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        return elements.stream().anyMatch(e -> e.blocksLine(from, to));
    }

    /**
     * Returns {@code true} if the point lies inside a reed clump. Predators that live in
     * reeds (snakes) and predators that must avoid them (fish) ask this question.
     *
     * @param point the point to test
     * @return whether a reed clump covers the point
     * @throws NullPointerException if {@code point} is {@code null}
     */
    public boolean isInReed(Vec2 point) {
        Objects.requireNonNull(point, "point");
        for (Element element : elements) {
            if (element instanceof Reed && point.distanceTo(element.getPosition()) <= element.getRadius()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the elements in turn order: top to bottom, then left to right. The frog is not
     * included. The result is a new list, so the stored order is never changed.
     *
     * @return a new list in turn order
     */
    public List<Element> elementsInOrder() {
        return ElementOrder.sorted(elements);
    }

    /**
     * Adds an element to the pond. Does not check {@link #isFree}; the caller does that.
     *
     * @param element the element to add
     * @throws NullPointerException if {@code element} is {@code null}
     */
    public void add(Element element) {
        elements.add(Objects.requireNonNull(element, "element"));
    }

    /**
     * Removes an element from the pond, if it is present.
     *
     * @param element the element to remove
     */
    public void remove(Element element) {
        elements.remove(element);
    }

    /**
     * Adds a ripple that has just started.
     *
     * @param ripple the ripple to add
     * @throws NullPointerException if {@code ripple} is {@code null}
     */
    public void addRipple(Ripple ripple) {
        ripples.add(Objects.requireNonNull(ripple, "ripple"));
    }

    /**
     * Returns the active ripples as a read-only view.
     *
     * @return the ripples, unmodifiable
     */
    public List<Ripple> ripples() {
        return Collections.unmodifiableList(ripples);
    }


     /**
      * Removes the ripples that have faded out. Called once per turn, after the ripples spread.
      */
     public void removeFadedRipples() {
         ripples.removeIf(Ripple::isFaded);
     }
}