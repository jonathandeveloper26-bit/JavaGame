package ripplepond.model.level;

import java.util.List;
import java.util.Objects;

import ripplepond.model.Balance;
import ripplepond.model.element.Element;
import ripplepond.model.element.Frog;
import ripplepond.model.element.pickup.Pickup;
import ripplepond.model.event.EventType;
import ripplepond.model.event.GameEvent;
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
     * @throws UnsupportedOperationException if the action asks for a throw, until the
     *         throw step is switched on
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
     * passes it this turn, then reacts to the combined push (GDD 3.1).
     * <p>
     * Each ripple pushes one push distance directly away from its source. The pushes are
     * added, and the total is capped at one push distance, so two ripples never push an
     * object twice as far, and ripples from opposite sides partly or fully cancel. The
     * element then decides what the push does to it, through {@link Element#reactTo}: a
     * floating object moves, a turtle wakes, and anything else ignores it. Elements that
     * no ripple passed are not asked.
     *
     * @param level the level being played
     */
    protected void objectsReact(Level level) {
        Pond pond = level.getPond();
        List<Ripple> ripples = pond.ripples();
        for (Element element : pond.elementsInOrder()) {
            Vec2 summed = new Vec2(0, 0);
            boolean passed = false;
            for (Ripple ripple : ripples) {
                if (ripple.passes(element, pond)) {
                    Vec2 push = ripple.pushDirection(element).scale(Balance.RIPPLE_PUSH_DISTANCE);
                    summed = summed.plus(push);
                    passed = true;
                }
            }
            if (passed) {
                element.reactTo(pushStep(summed), level);
            }
        }
    }

    /**
     * Caps a combined push at one push distance ({@link Balance#RIPPLE_PUSH_DISTANCE}),
     * keeping its direction. A push already shorter, because ripples partly cancel, is
     * kept as it is; pushes that cancel fully give no move.
     *
     * @param summed the sum of the pushes of every ripple that passed, in play-area widths
     * @return the push to apply; the zero vector when nothing should move
     */
    Vec2 pushStep(Vec2 summed) {
        double length = summed.length();
        if (length <= Balance.RIPPLE_PUSH_DISTANCE) {
            return summed;
        }
        return summed.normalised().scale(Balance.RIPPLE_PUSH_DISTANCE);
    }

    /**
     * Step 4: makes the frog's move if the action has one (GDD 4.2). The frog checks the
     * rules and the energy cost itself, and refuses a move it may not make.
     * <p>
     * A move that happens publishes {@code FROG_MOVED}, with the distance travelled as
     * its amount. Then the frog collects every pickup it touches: food and throwable
     * icons (GDD section 3, step 4). A refused move publishes nothing and collects
     * nothing.
     *
     * @param level  the level being played
     * @param action the action containing the move, if any
     */
    protected void frogMoves(Level level, TurnAction action) {
        if (!action.hasMove()) {
            return;
        }
        Pond pond = level.getPond();
        Frog frog = pond.getFrog();
        double distance = frog.getPosition().distanceTo(action.getMoveTarget());
        if (frog.move(action.getMoveMode(), action.getMoveTarget(), pond)) {
            level.publish(new GameEvent(EventType.FROG_MOVED, frog, distance));
            collectPickups(level, frog);
        }
    }

    /**
     * Gives the frog every pickup it touches. The list is a copy, so a pickup can remove
     * itself from the pond while it is collected.
     *
     * @param level the level being played
     * @param frog  the frog that just moved
     */
    private void collectPickups(Level level, Frog frog) {
        for (Element element : level.getPond().elementsInOrder()) {
            if (element instanceof Pickup pickup && frog.touches(pickup)) {
                pickup.collect(frog, level);
            }
        }
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