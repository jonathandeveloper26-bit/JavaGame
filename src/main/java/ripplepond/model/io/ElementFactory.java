package ripplepond.model.io;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ripplepond.model.Balance;
import ripplepond.model.element.Element;
import ripplepond.model.element.LilyPad;
import ripplepond.model.element.Reed;
import ripplepond.model.element.ThrowableKind;
import ripplepond.model.element.ThrownObject;
import ripplepond.model.element.Turtle;
import ripplepond.model.geometry.Vec2;

/**
 * Creates elements from the type names used in level files, such as {@code log} or
 * {@code lilypad} (GDD section 2.6, {@code docs/LEVEL_FORMAT.md} section 4).
 * <p>
 * This is the Factory pattern: the level loader only knows type names, and this class is
 * the one place that turns a name into the right class. Each name is registered with a
 * {@link Creator} and a default size, used when the level file gives no diameter.
 * <p>
 * Adding a new element type means adding one {@code register} line to the constructor:
 * Stephan adds {@code snake}, {@code bird} and {@code fish} (S6), and Xaryab adds
 * {@code fly}, {@code minnow} and {@code breadcrumb} (X1). Throwable icons use their own
 * line format ({@code icon <kind> <x> <y>}), so the loader handles them, not this class.
 */
public class ElementFactory {

    /**
     * Makes one kind of element at a position, with a size. Usually a constructor
     * reference, such as {@code LilyPad::new}.
     */
    @FunctionalInterface
    public interface Creator {

        /**
         * Creates the element.
         *
         * @param position the centre of the element, in play-area widths
         * @param size     the diameter of the element, in play-area widths
         * @return the new element
         */
        Element create(Vec2 position, double size);
    }

    /** What the factory knows about one type name. */
    private record Entry(double defaultSize, Creator creator) {
    }

    private final Map<String, Entry> types = new HashMap<>();

    /**
     * Creates a factory that knows every element type written so far.
     */
    public ElementFactory() {
        register("twig", Balance.TWIG_SIZE,
                (position, size) -> new ThrownObject(position, ThrowableKind.TWIG, size));
        register("plank", Balance.PLANK_SIZE,
                (position, size) -> new ThrownObject(position, ThrowableKind.PLANK, size));
        register("log", Balance.LOG_SIZE,
                (position, size) -> new ThrownObject(position, ThrowableKind.LOG, size));
        register("lilypad", Balance.LILY_PAD_SIZE, LilyPad::new);
        register("turtle", Balance.TURTLE_SIZE, Turtle::new);
        register("reed", Balance.REED_SIZE, Reed::new);
    }

    /**
     * Teaches the factory a new type name.
     *
     * @param name        the type name used in level files, in lower case
     * @param defaultSize the diameter used when a level file gives none; greater than 0
     * @param creator     makes the element
     * @throws NullPointerException     if {@code name} or {@code creator} is {@code null}
     * @throws IllegalArgumentException if the name is already registered, or the default
     *                                  size is not greater than 0
     */
    public final void register(String name, double defaultSize, Creator creator) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(creator, "creator");
        if (!(defaultSize > 0)) {
            throw new IllegalArgumentException("default size must be greater than 0: " + defaultSize);
        }
        if (types.containsKey(name)) {
            throw new IllegalArgumentException("type already registered: " + name);
        }
        types.put(name, new Entry(defaultSize, creator));
    }

    /**
     * Returns {@code true} if the factory knows a type name.
     *
     * @param typeName the name to look up
     * @return whether {@link #create} accepts the name
     */
    public boolean knows(String typeName) {
        return types.containsKey(typeName);
    }

    /**
     * Returns the diameter a type has when a level file gives none.
     *
     * @param typeName the type name
     * @return the default diameter, in play-area widths
     * @throws LevelFormatException if the type name is unknown
     */
    public double defaultSize(String typeName) throws LevelFormatException {
        return entryFor(typeName).defaultSize();
    }

    /**
     * Creates an element of a named type.
     *
     * @param typeName the type name from the level file, such as {@code log}
     * @param position the centre of the element, in play-area widths
     * @param size     the diameter of the element, in play-area widths; greater than 0
     * @return the new element
     * @throws LevelFormatException     if the type name is unknown
     * @throws NullPointerException     if {@code position} is {@code null}
     * @throws IllegalArgumentException if {@code size} is not greater than 0
     */
    public Element create(String typeName, Vec2 position, double size) throws LevelFormatException {
        Objects.requireNonNull(position, "position");
        return entryFor(typeName).creator().create(position, size);
    }

    private Entry entryFor(String typeName) throws LevelFormatException {
        Entry entry = types.get(typeName);
        if (entry == null) {
            throw new LevelFormatException("unknown element type '" + typeName + "'");
        }
        return entry;
    }
}
