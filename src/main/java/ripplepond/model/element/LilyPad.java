package ripplepond.model.element;

import ripplepond.model.geometry.Vec2;

/**
 * A lily pad: a floating object that a level places in the river (GDD section 2.2). The
 * frog can stand on it, and ripples push it like any other floating object.
 * <p>
 * It adds nothing to {@link FloatingObject}; it is a class of its own so the game and
 * the view can tell a pad from a thrown object, and so the pad push rules in J8 have a
 * place to go.
 */
public final class LilyPad extends FloatingObject {

    /**
     * Creates a lily pad.
     *
     * @param position the centre of the pad, in play-area widths
     * @param size     the diameter of the pad, in play-area widths; greater than 0
     * @throws NullPointerException     if {@code position} is {@code null}
     * @throws IllegalArgumentException if {@code size} is not greater than 0
     */
    public LilyPad(Vec2 position, double size) {
        super(position, size);
    }
}
