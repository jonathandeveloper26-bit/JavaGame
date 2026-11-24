package ripplepond.model.element.pickup;

import ripplepond.model.element.AbstractElement;
import ripplepond.model.element.Frog;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.Level;

/**
 * Something the frog picks up by touching it: food (GDD section 2.5) or a throwable icon
 * (GDD section 2.2).
 * <p>
 * Every pickup answers the same question, "what happens when the frog collects you?",
 * in its own way, so {@link #collect} is abstract. {@code Food} (Xaryab, X1) restores
 * the frog's health or energy; {@code ThrowableIcon} (J8) adds an object to the throw
 * bag. Code that handles the frog touching a pickup only needs this class, not the
 * concrete kinds.
 */
public abstract class Pickup extends AbstractElement {

    /**
     * Creates a pickup.
     *
     * @param position the centre of the pickup, in play-area widths
     * @param size     the diameter of the pickup, in play-area widths; greater than 0
     * @throws NullPointerException     if {@code position} is {@code null}
     * @throws IllegalArgumentException if {@code size} is not greater than 0
     */
    protected Pickup(Vec2 position, double size) {
        super(position, size);
    }

    /**
     * Gives this pickup to the frog. Each kind of pickup decides what that means, and
     * is responsible for removing itself from the level once collected.
     *
     * @param frog  the frog that touched the pickup
     * @param level the level the pickup is in
     */
    public abstract void collect(Frog frog, Level level);
}
