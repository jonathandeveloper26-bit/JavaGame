package ripplepond.model.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import ripplepond.model.Balance;
import ripplepond.model.element.Element;
import ripplepond.model.element.Frog;
import ripplepond.model.event.GameEvent;
import ripplepond.model.event.PondListener;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.progress.Rank;
import ripplepond.model.ripple.CircularSpread;
import ripplepond.model.ripple.Ripple;

/**
 * Tests for the turn engine's step order, the ripple steps, and determinism.
 * <p>
 * Tests that need J4 (throws), J6 (frog moves), J8 (pushes that move elements) or S6
 * (predators) are commented at the end of the file and are added back when those merge.
 */
class TurnEngineTest {

    private static final double DELTA = 1e-12;
    private static final TurnAction NOTHING = new TurnAction(null, null, null, null);

    /** A plain element for these tests. It never moves and ignores ripples. */
    private static final class PlainElement implements Element {
        private final Vec2 position;
        private final double size;

        PlainElement(Vec2 position, double size) {
            this.position = position;
            this.size = size;
        }

        @Override
        public Vec2 getPosition() {
            return position;
        }

        @Override
        public double getSize() {
            return size;
        }

        @Override
        public double getRadius() {
            return size / 2;
        }

        @Override
        public boolean touches(Element other) {
            return distanceTo(other) <= getRadius() + other.getRadius();
        }

        @Override
        public double distanceTo(Element other) {
            return position.distanceTo(other.getPosition());
        }

        @Override
        public void reactTo(Ripple ripple, Pond pond) {
            // Stub: ignores ripples.
        }
    }

    /** Records every event it receives, in the order received. */
    private static final class RecordingListener implements PondListener {
        final List<GameEvent> received = new ArrayList<>();

        @Override
        public void onEvent(GameEvent event) {
            received.add(event);
        }
    }

    private static Pond emptyPond() {
        // A pond with no banks is all water. The frog sits in it, away from the far bank,
        // so the end rules keep the level in progress (J3). It is not one of the pond's
        // elements, so ripples never push it.
        return new Pond(Pond.WIDTH, Pond.HEIGHT, new Frog(new Vec2(0.5, 0.5), Rank.RANK_1));
    }

    private static Level levelOn(Pond pond) {
        return new Level(pond, new ThrowBag(), 40, new InProgress());
    }

    private static Ripple rippleAt(Vec2 source) {
        return new Ripple(source, new PlainElement(source, 0.04), new CircularSpread());
    }

    private static List<Double> radii(Pond pond) {
        List<Double> result = new ArrayList<>();
        for (Ripple ripple : pond.ripples()) {
            result.add(ripple.getRadius());
        }
        return result;
    }

    @Test
    void nullLevelOrActionIsRejected() {
        TurnEngine engine = new TurnEngine();

        assertThrows(NullPointerException.class, () -> engine.runTurn(null, NOTHING));
        assertThrows(NullPointerException.class, () -> engine.runTurn(levelOn(emptyPond()), null));
    }

    @Test
    void spreadRipplesTakesOneStepForEachRipple() {
        Pond pond = emptyPond();
        Ripple ripple = rippleAt(new Vec2(0.5, 0.3));
        pond.addRipple(ripple);

        new TurnEngine().spreadRipples(pond);

        assertEquals(Balance.RIPPLE_GROWTH_PER_TURN, ripple.getRadius(), DELTA);
        assertEquals(1, pond.ripples().size());
    }

    @Test
    void rippleSpreadsBeforeObjectsReactInTheSameTurn() {
        Pond pond = emptyPond();
        Ripple ripple = rippleAt(new Vec2(0.5, 0.3));
        pond.addRipple(ripple);
        // Centre 0.1 from the source, radius 0.05, so the nearest edge is 0.05 away.
        // The first spread step reaches 0.08, so the ring crosses this edge in turn 1.
        Element near = new PlainElement(new Vec2(0.5, 0.4), 0.1);
        pond.add(near);

        levelOn(pond).playTurn(NOTHING);

        assertFalse(ripple.passes(near, pond), "the engine already recorded this pass in turn 1");
    }

    @Test
    void theLastTurnOfARippleStillReachesElementsThenItIsRemoved() {
        Pond pond = emptyPond();
        Ripple ripple = rippleAt(new Vec2(0.5, 0.1));
        pond.addRipple(ripple);
        // Centre 0.2 from the source, radius 0.02, so the nearest edge is 0.18 away.
        // The ring reaches 0.24 only on the third spread step.
        Element far = new PlainElement(new Vec2(0.5, 0.3), 0.04);
        pond.add(far);
        Level level = levelOn(pond);

        for (int turn = 0; turn < Balance.RIPPLE_LIFETIME_TURNS; turn++) {
            level.playTurn(NOTHING);
        }

        assertTrue(pond.ripples().isEmpty(), "the ripple is removed at the end of its last turn");
        assertFalse(ripple.passes(far, pond), "the third turn's ring pass was recorded");
    }

    @Test
    void twoRipplesPassingTheSameElementInOneTurnBothRecordIt() {
        Pond pond = emptyPond();
        Ripple north = rippleAt(new Vec2(0.5, 0.3));
        Ripple south = rippleAt(new Vec2(0.5, 0.7));
        pond.addRipple(north);
        pond.addRipple(south);
        Element target = new PlainElement(new Vec2(0.5, 0.5), 0.04);
        pond.add(target);
        Level level = levelOn(pond);

        for (int turn = 0; turn < Balance.RIPPLE_LIFETIME_TURNS; turn++) {
            level.playTurn(NOTHING);
        }

        assertFalse(north.passes(target, pond));
        assertFalse(south.passes(target, pond));
    }

    @Test
    void pushIsCappedAtOnePushDistance() {
        Vec2 step = new TurnEngine().pushStep(new Vec2(2, 0));

        assertEquals(Balance.RIPPLE_PUSH_DISTANCE, step.getX(), DELTA);
        assertEquals(0, step.getY(), DELTA);
    }

    @Test
    void opposingPushesCancelToNoMove() {
        Vec2 step = new TurnEngine().pushStep(new Vec2(0, 0));

        assertEquals(0, step.length(), DELTA);
    }

    @Test
    void smallSumMovesOnlyItsOwnLength() {
        Vec2 step = new TurnEngine().pushStep(new Vec2(0.02, 0));

        assertEquals(0.02, step.getX(), DELTA);
        assertEquals(0, step.getY(), DELTA);
    }

    @Test
    void sameStartAndSameActionsGiveTheSameRiverEveryTurn() {
        Level a = levelOn(pondWithOneRippleAndOneElement());
        Level b = levelOn(pondWithOneRippleAndOneElement());
        RecordingListener listenerA = new RecordingListener();
        RecordingListener listenerB = new RecordingListener();
        a.addListener(listenerA);
        b.addListener(listenerB);

        for (int turn = 0; turn < Balance.RIPPLE_LIFETIME_TURNS; turn++) {
            a.playTurn(NOTHING);
            b.playTurn(NOTHING);
            assertEquals(radii(a.getPond()), radii(b.getPond()));
        }
        assertEquals(listenerA.received.size(), listenerB.received.size());
    }

    private static Pond pondWithOneRippleAndOneElement() {
        Pond pond = emptyPond();
        pond.addRipple(rippleAt(new Vec2(0.5, 0.3)));
        pond.add(new PlainElement(new Vec2(0.5, 0.4), 0.1));
        return pond;
    }

    @Test
    void emptyTurnPublishesNoEvents() {
        Level level = levelOn(emptyPond());
        RecordingListener listener = new RecordingListener();
        level.addListener(listener);

        level.playTurn(NOTHING);

        assertTrue(listener.received.isEmpty());
    }

    @Test
    void playTurnCountsTurns() {
        Level level = levelOn(emptyPond());

        level.playTurn(NOTHING);
        level.playTurn(NOTHING);

        assertEquals(2, level.getTurn());
    }

    // TODO(J4) activation: throws. Add once ThrowableKind.size(), ThrowBag and ThrownObject exist.
    // A throw onto an occupied spot leaves the bag unchanged; a free spot adds the object,
    // starts its ripple and publishes OBJECT_THROWN.
    //
    // TODO(J6) activation: frog moves. Add once Frog.move and MoveMode.isLegal exist.
    // A legal move publishes FROG_MOVED; an illegal or unaffordable move leaves the frog where it is.
    //
    // TODO(J8) activation: pushes that move elements. Add once FloatingObject.push exists.
    // A floating object moves by the pushStep displacement and publishes OBJECT_PUSHED.
    //
    // TODO(S6) activation: predators. Add once Predator and its hook exist.
}