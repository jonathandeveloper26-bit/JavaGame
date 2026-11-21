package ripplepond.model.level;

import ripplepond.model.geometry.Vec2;
import ripplepond.model.movement.MoveMode;
import ripplepond.model.element.ThrowableKind;

/**
 * What the player asked for in one turn: an optional throw, an optional move, or both.
 * Immutable, so a queued action cannot change after the player has chosen it.
 */
public final class TurnAction {

    private final ThrowableKind throwKind;
    private final Vec2 throwTarget;
    private final MoveMode moveMode;
    private final Vec2 moveTarget;

    /**
     * Creates an action. Pass {@code null} for any part the player did not choose.
     *
     * @param throwKind   the object to throw, or {@code null} for no throw
     * @param throwTarget where the object lands, or {@code null} for no throw
     * @param moveMode    how the frog moves, or {@code null} for no move
     * @param moveTarget  where the frog moves to, or {@code null} for no move
     */
    public TurnAction(ThrowableKind throwKind, Vec2 throwTarget,
                      MoveMode moveMode, Vec2 moveTarget) {
        this.throwKind = throwKind;
        this.throwTarget = throwTarget;
        this.moveMode = moveMode;
        this.moveTarget = moveTarget;
    }

    /** Returns the object to throw, or {@code null} if there is no throw. */
    public ThrowableKind getThrowKind() {
        return throwKind;
    }

    /** Returns where the object lands, or {@code null} if there is no throw. */
    public Vec2 getThrowTarget() {
        return throwTarget;
    }

    /** Returns how the frog moves, or {@code null} if there is no move. */
    public MoveMode getMoveMode() {
        return moveMode;
    }

    /** Returns where the frog moves to, or {@code null} if there is no move. */
    public Vec2 getMoveTarget() {
        return moveTarget;
    }

    /** Returns {@code true} if this action includes a throw. */
    public boolean hasThrow() {
        return throwKind != null;
    }

    /** Returns {@code true} if this action includes a move. */
    public boolean hasMove() {
        return moveMode != null;
    }
}
