package ripplepond.model.element.pickup;

import java.util.Objects;

import ripplepond.model.element.Frog;
import ripplepond.model.event.EventType;
import ripplepond.model.event.GameEvent;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.Level;

/**
 * Food the frog eats by touching it, to get back health, energy or both (GDD section
 * 2.5).
 * <p>
 * Eating uses the Template method pattern. {@link #collect} is the fixed part, written
 * once here and declared {@code final}: the food does its good to the frog, leaves the
 * pond, and announces {@code FOOD_EATEN}. Each kind of food fills in only
 * {@link #applyTo}, which says what it restores. The frog itself keeps health and energy
 * within their maximums.
 */
public abstract class Food extends Pickup {

    /**
     * Creates food.
     *
     * @param position the centre of the food, in play-area widths
     * @param size     the diameter of the food, in play-area widths; greater than 0
     * @throws NullPointerException     if {@code position} is {@code null}
     * @throws IllegalArgumentException if {@code size} is not greater than 0
     */
    protected Food(Vec2 position, double size) {
        super(position, size);
    }

    /**
     * The frog eats this food: it restores what {@link #applyTo} says, the food is
     * removed from the pond, and {@code FOOD_EATEN} is published with the food as the
     * subject and an amount of 1.
     *
     * @param frog  the frog that touched the food
     * @param level the level the food is in
     * @throws NullPointerException if {@code frog} or {@code level} is {@code null}
     */
    @Override
    public final void collect(Frog frog, Level level) {
        Objects.requireNonNull(frog, "frog");
        Objects.requireNonNull(level, "level");
        applyTo(frog);
        level.getPond().remove(this);
        level.publish(new GameEvent(EventType.FOOD_EATEN, this, 1));
    }

    /**
     * What eating this food does for the frog: the health and energy it restores.
     *
     * @param frog the frog eating it
     */
    protected abstract void applyTo(Frog frog);
}
