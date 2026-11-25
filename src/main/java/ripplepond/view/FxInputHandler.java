package ripplepond.view;

import ripplepond.controller.GameController;
import ripplepond.model.element.Frog;
import ripplepond.model.element.ThrowableKind;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.io.LevelFormatException;
import ripplepond.model.level.Level;
import ripplepond.model.level.Pond;
import ripplepond.model.level.TurnAction;
import ripplepond.model.movement.Hop;
import ripplepond.model.movement.MoveMode;
import ripplepond.model.movement.Swim;
import ripplepond.model.movement.Walk;

/**
 * Turns the player's clicks into one {@link TurnAction} per turn (GDD section 3).
 * <p>
 * The player chooses either a throw (an object from the bag, then a point on the water)
 * or a move (walk, hop or swim, then a point), and presses End turn to play the turn.
 * Only one throw and one move can be chosen per turn, which matches
 * {@link TurnAction}. The handler checks what it can, so the player gets a message
 * instead of a silent refusal; the model still makes the final decision.
 */
final class FxInputHandler {

    static final MoveMode WALK = new Walk();
    static final MoveMode HOP = new Hop();
    static final MoveMode SWIM = new Swim();

    /** What the player is picking in the play area. */
    private enum Choice {
        THROW, MOVE
    }

    /** The choices made so far this turn, for the view to draw. */
    record Pending(ThrowableKind throwKind, Vec2 throwTarget, Vec2 moveTarget) {
    }

    private final GameController controller;
    private final FxLevelView view;

    private Choice choice = Choice.MOVE;
    private ThrowableKind throwKind;
    private Vec2 throwTarget;
    private MoveMode moveMode = WALK;
    private Vec2 moveTarget;

    FxInputHandler(GameController controller, FxLevelView view) {
        this.controller = controller;
        this.view = view;
    }

    /** Returns the choices made so far this turn. */
    Pending pending() {
        return new Pending(throwKind, throwTarget, moveTarget);
    }

    /**
     * The player picks an object from the bag to throw next.
     *
     * @param kind the kind of object
     */
    void chooseThrow(ThrowableKind kind) {
        Level level = controller.getLevel();
        if (level == null || !level.getBag().has(kind)) {
            view.say("No " + kind.name().toLowerCase() + " left in the bag.");
            return;
        }
        choice = Choice.THROW;
        throwKind = kind;
        throwTarget = null;
        view.say("Click the water to drop the " + kind.name().toLowerCase() + ".");
        view.refresh();
    }

    /**
     * The player picks how the frog moves next.
     *
     * @param mode walk, hop or swim
     */
    void chooseMove(MoveMode mode) {
        choice = Choice.MOVE;
        moveMode = mode;
        moveTarget = null;
        view.say("Click where the frog should go by " + mode.toString().toLowerCase() + ".");
        view.refresh();
    }

    /**
     * The player clicked a point in the play area.
     *
     * @param point the point, in play-area units
     */
    void clickPlayArea(Vec2 point) {
        Level level = controller.getLevel();
        if (level == null) {
            return;
        }
        Pond pond = level.getPond();
        if (choice == Choice.THROW) {
            if (throwKind == null) {
                view.say("Pick an object from the bag first.");
            } else if (!pond.isFree(point, throwKind.size())) {
                view.say("No room for it there. Try open water.");
            } else {
                throwTarget = point;
            }
        } else {
            moveTarget = point;
            String problem = moveProblem(pond.getFrog(), pond, moveMode, point);
            view.say(problem == null ? "Press End turn to move." : problem);
        }
        view.refresh();
    }

    /** Plays the turn with whatever the player has chosen, then refreshes the screen. */
    void endTurn() {
        Level level = controller.getLevel();
        if (level == null || level.getState().isOver()) {
            return;
        }
        Pond pond = level.getPond();
        Vec2 move = moveTarget;
        if (move != null && moveProblem(pond.getFrog(), pond, moveMode, move) != null) {
            view.say("That move was dropped, so the turn was played without it.");
            move = null;
        }
        boolean throwing = throwTarget != null;
        TurnAction action = new TurnAction(
                throwing ? throwKind : null,
                throwing ? throwTarget : null,
                move == null ? null : moveMode,
                move);
        controller.submit(action);
        clearPending();
        view.afterTurn();
    }

    /** Restarts the level from its file, keeping experience (GDD 4.5). */
    void reset() {
        try {
            controller.reset();
            clearPending();
            view.say("The level was restarted.");
            view.refresh();
        } catch (LevelFormatException e) {
            view.say("Could not reload the level: " + e.getMessage());
        }
    }

    private void clearPending() {
        throwTarget = null;
        moveTarget = null;
    }

    /**
     * Explains why a move cannot be made, or returns {@code null} if it can.
     * This is a preview; the model makes the real decision when the turn is played.
     */
    private static String moveProblem(Frog frog, Pond pond, MoveMode mode, Vec2 target) {
        if (!mode.isLegal(frog, target, pond)) {
            return "The frog cannot " + mode.toString().toLowerCase() + " there.";
        }
        if (!frog.canAfford(mode, target)) {
            return "The frog does not have enough energy for that.";
        }
        return null;
    }
}