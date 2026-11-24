package ripplepond.model.level;

import java.util.List;
import java.util.Objects;

import ripplepond.model.Balance;
import ripplepond.model.element.Element;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.ripple.Ripple;

/**
 * Runs one turn as five steps in a fixed order (GDD section 3): throw, ripples spread,
 * objects react, frog moves, predators act. Faded ripples are removed at the end of the
 * turn, after the objects have reacted, so a ripple's last spread step still pushes.
 * <p>
 * Template method: {@link #runTurn} fixes the order and each step fills in its part
 * (GDD 8.3). Parts that need other members' work are marked {@code TODO(Jn)} or
 * {@code TODO(S6)} and stay commented until those tasks merge.
 */
public class TurnEngine {

    /**
     * Runs the five steps of a turn in order, then removes the ripples that have faded.
     *
     * @param level  the level being played
     * @param action what the player asked for this turn
     * @throws NullPointerException if either argument is {@code null}
     * @throws UnsupportedOperationException if the action asks for a throw or a frog move
     *         before J4 or J6 is merged
     */
    public void runTurn(Level level, TurnAction action) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(action, "action");
        throwStep(level, action);
        spreadRipples(level.getPond());
        objectsReact(level);
        frogMoves(level, action);
        predatorsAct(level);
        level.getPond().removeFadedRipples();
    }

    /**
     * Step 1: places the thrown object and starts its ripple. A throw onto an occupied spot
     * is refused before the bag is touched, so no object is lost.
     *
     * @param level  the level being played
     * @param action the action containing the throw, if any
     * @throws UnsupportedOperationException if the action has a throw, until J4 is merged
     */
    protected void throwStep(Level level, TurnAction action) {
        if (!action.hasThrow()) {
            return;
        }
        // TODO(J4): once ThrowableKind.size(), ThrowBag.has/take and ThrownObject exist,
        // replace the throw below with:
        //
        // Pond pond = level.getPond();
        // Vec2 target = action.getThrowTarget();
        // double size = action.getThrowKind().size();
        // if (!pond.isFree(target, size)) {
        //     return;
        // }
        // if (!level.getBag().has(action.getThrowKind())) {
        //     return;
        // }
        // ThrownObject thrown = level.getBag().take(action.getThrowKind());
        // pond.add(thrown);
        // pond.addRipple(new Ripple(target, thrown, new CircularSpread()));
        // level.publish(new GameEvent(EventType.OBJECT_THROWN, thrown, size));
        throw new UnsupportedOperationException("A throw waits for J4: ThrowBag and ThrowableKind.size()");
    }

    /**
     * Step 2: every active ripple takes one spread step. Faded ripples are removed later, at
     * the end of the turn.
     *
     * @param pond the pond holding the ripples
     */
    protected void spreadRipples(Pond pond) {
        for (Ripple ripple : pond.ripples()) {
            ripple.advance(pond);
        }
    }

    /**
     * Step 3: each element, in turn order, collects the push from every ripple whose ring
     * passes it this turn. The directions are summed first, then the element moves at most
     * one push distance (GDD 3.1), so two ripples never push an object twice as far.
     * <p>
     * This uses {@link Ripple#passes} and {@link Ripple#pushDirection} directly rather than
     * {@code Element.reactTo}, because {@code reactTo} applies each push as it comes, which
     * would break the summing rule.
     *
     * @param level the level being played
     */
    protected void objectsReact(Level level) {
        Pond pond = level.getPond();
        List<Ripple> ripples = pond.ripples();
        for (Element element : pond.elementsInOrder()) {
            Vec2 summed = new Vec2(0, 0);
            boolean pushed = false;
            for (Ripple ripple : ripples) {
                if (ripple.passes(element, pond)) {
                    summed = summed.plus(ripple.pushDirection(element));
                    pushed = true;
                }
            }
            if (pushed) {
                applyPush(level, element, summed);
            }
        }
    }

    /**
     * Works out how far an element moves for a summed push: along the direction of the sum,
     * by at most {@link Balance#RIPPLE_PUSH_DISTANCE}. Opposing pushes that cancel give no move.
     *
     * @param summed the sum of the unit push directions from every ripple that passed
     * @return the displacement to apply; the zero vector when nothing should move
     */
    Vec2 pushStep(Vec2 summed) {
        double length = summed.length();
        if (length == 0.0) {
            return new Vec2(0, 0);
        }
        double distance = Math.min(Balance.RIPPLE_PUSH_DISTANCE, length);
        return summed.normalised().scale(distance);
    }

    /**
     * Applies a push to one element. Nothing moves until J8 is merged, so the displacement is
     * computed and then discarded for now.
     *
     * @param level  the level being played
     * @param element the element receiving the push
     * @param summed  the sum of the push directions that passed this element
     */
    private void applyPush(Level level, Element element, Vec2 summed) {
        Vec2 step = pushStep(summed);
        if (step.length() == 0.0) {
            return;
        }
        // TODO(J8): once FloatingObject.push, Turtle wake-up and ICON/OBJECT events exist:
        //
        // if (element instanceof FloatingObject floating) {
        //     double distance = step.length();
        //     if (floating.push(step.normalised(), distance, level.getPond())) {
        //         level.publish(new GameEvent(EventType.OBJECT_PUSHED, element, distance));
        //     }
        // }
        // Turtles are woken by J8's own rule (Turtle.onRipple), not here.
    }

    /**
     * Step 4: makes the frog's move if the action has one. The frog checks the rules and the
     * energy cost itself (J6).
     *
     * @param level  the level being played
     * @param action the action containing the move, if any
     * @throws UnsupportedOperationException if the action has a move, until J6 is merged
     */
    protected void frogMoves(Level level, TurnAction action) {
        if (!action.hasMove()) {
            return;
        }
        // TODO(J6): once Frog.move and MoveMode.isLegal exist, replace the throw with:
        //
        // Frog frog = level.getPond().getFrog();
        // if (frog.move(action.getMoveMode(), action.getMoveTarget(), level.getPond())) {
        //     level.publish(new GameEvent(EventType.FROG_MOVED, frog, 0));
        // }
        throw new UnsupportedOperationException("A frog move waits for J6: Frog.move and MoveMode.isLegal");
    }

    /**
     * Step 5: predators move and attack. Empty until S6 (GDD 2.4).
     *
     * @param level the level being played
     */
    protected void predatorsAct(Level level) {
        // TODO(S6): predators move every second turn and attack on any turn in reach.
    }
}