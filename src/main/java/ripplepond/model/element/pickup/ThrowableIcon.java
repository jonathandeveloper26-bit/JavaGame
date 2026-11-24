package ripplepond.model.element.pickup;

import java.util.Objects;

import ripplepond.model.Balance;
import ripplepond.model.element.Frog;
import ripplepond.model.element.ThrowableKind;
import ripplepond.model.event.EventType;
import ripplepond.model.event.GameEvent;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.Level;

/**
 * An icon in the level that gives the frog one object to throw (GDD section 2.2). The
 * frog gathers it by touching it, and the object of the icon's kind goes into the throw
 * bag.
 */
public final class ThrowableIcon extends Pickup {

    private final ThrowableKind kind;

    /**
     * Creates an icon of the usual size, {@link Balance#ICON_SIZE}.
     *
     * @param position the centre of the icon, in play-area widths
     * @param kind     the kind of object the icon gives
     * @throws NullPointerException if {@code position} or {@code kind} is {@code null}
     */
    public ThrowableIcon(Vec2 position, ThrowableKind kind) {
        this(position, kind, Balance.ICON_SIZE);
    }

    /**
     * Creates an icon with a chosen size, as a level file may give one.
     *
     * @param position the centre of the icon, in play-area widths
     * @param kind     the kind of object the icon gives
     * @param size     the diameter of the icon, in play-area widths; greater than 0
     * @throws NullPointerException     if {@code position} or {@code kind} is {@code null}
     * @throws IllegalArgumentException if {@code size} is not greater than 0
     */
    public ThrowableIcon(Vec2 position, ThrowableKind kind, double size) {
        super(position, size);
        this.kind = Objects.requireNonNull(kind, "kind");
    }

    /**
     * Returns the kind of object this icon gives.
     *
     * @return the kind
     */
    public ThrowableKind getKind() {
        return kind;
    }

    /**
     * Gives the frog the icon's object: adds one of its kind to the throw bag, removes
     * the icon from the pond, and publishes {@code ICON_GATHERED} with the icon as the
     * subject and an amount of 1.
     *
     * @param frog  the frog that touched the icon
     * @param level the level the icon is in
     * @throws NullPointerException if {@code frog} or {@code level} is {@code null}
     */
    @Override
    public void collect(Frog frog, Level level) {
        Objects.requireNonNull(frog, "frog");
        Objects.requireNonNull(level, "level");
        level.getBag().add(kind);
        level.getPond().remove(this);
        level.publish(new GameEvent(EventType.ICON_GATHERED, this, 1));
    }
}
