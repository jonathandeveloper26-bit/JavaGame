package ripplepond.model.element;

import java.util.Objects;

import ripplepond.model.geometry.Vec2;

/**
 * An object the player threw into the river, or one a level places there: a twig, a
 * plank or a log (GDD section 2.2). It floats where it lands and the frog can stand on
 * it.
 */
public final class ThrownObject extends FloatingObject {

    private final ThrowableKind kind;

    /**
     * Creates a thrown object with the usual size for its kind. This is what a throw
     * creates.
     *
     * @param position the centre of the object, in play-area widths
     * @param kind     what kind of object it is
     * @throws NullPointerException if {@code position} or {@code kind} is {@code null}
     */
    public ThrownObject(Vec2 position, ThrowableKind kind) {
        this(position, kind, Objects.requireNonNull(kind, "kind").size());
    }

    /**
     * Creates a thrown object with a chosen size. A level file may give an object a size
     * other than the usual one for its kind (see {@code docs/LEVEL_FORMAT.md}).
     *
     * @param position the centre of the object, in play-area widths
     * @param kind     what kind of object it is
     * @param size     the diameter of the object, in play-area widths; greater than 0
     * @throws NullPointerException     if {@code position} or {@code kind} is {@code null}
     * @throws IllegalArgumentException if {@code size} is not greater than 0
     */
    public ThrownObject(Vec2 position, ThrowableKind kind, double size) {
        super(position, size);
        this.kind = Objects.requireNonNull(kind, "kind");
    }

    /**
     * Returns what kind of object this is.
     *
     * @return the kind
     */
    public ThrowableKind getKind() {
        return kind;
    }
}
