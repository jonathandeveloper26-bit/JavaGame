package ripplepond.model.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import ripplepond.model.Balance;
import ripplepond.model.element.Element;
import ripplepond.model.element.Frog;
import ripplepond.model.element.ThrowableKind;
import ripplepond.model.element.ThrownObject;
import ripplepond.model.element.pickup.Pickup;
import ripplepond.model.event.EventType;
import ripplepond.model.event.GameEvent;
import ripplepond.model.event.PondListener;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.movement.Hop;
import ripplepond.model.movement.MoveMode;
import ripplepond.model.movement.Swim;
import ripplepond.model.movement.Walk;
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

    // Throws (J4, switched on in J9). The pond is all water; the frog is at (0.5, 0.5).

    private static final Vec2 THROW_SPOT = new Vec2(0.25, 0.25);

    private static Level levelWithBag(Pond pond, ThrowableKind... kinds) {
        ThrowBag bag = new ThrowBag();
        for (ThrowableKind kind : kinds) {
            bag.add(kind);
        }
        return new Level(pond, bag, 40, new InProgress());
    }

    private static TurnAction throwAt(ThrowableKind kind, Vec2 target) {
        return new TurnAction(kind, target, null, null);
    }

    @Test
    void aThrowLandsTheObjectAndTakesItFromTheBag() {
        Pond pond = emptyPond();
        Level level = levelWithBag(pond, ThrowableKind.PLANK);

        level.playTurn(throwAt(ThrowableKind.PLANK, THROW_SPOT));

        assertEquals(1, pond.elementsInOrder().size());
        ThrownObject plank = (ThrownObject) pond.elementsInOrder().get(0);
        assertEquals(ThrowableKind.PLANK, plank.getKind());
        assertEquals(THROW_SPOT, plank.getPosition());
        assertFalse(level.getBag().has(ThrowableKind.PLANK));
    }

    @Test
    void aThrowStartsARippleThatSpreadsInTheSameTurn() {
        Pond pond = emptyPond();
        Level level = levelWithBag(pond, ThrowableKind.TWIG);

        level.playTurn(throwAt(ThrowableKind.TWIG, THROW_SPOT));

        assertEquals(1, pond.ripples().size());
        Ripple ripple = pond.ripples().get(0);
        assertEquals(THROW_SPOT, ripple.getSource());
        assertEquals(Balance.RIPPLE_GROWTH_PER_TURN, ripple.getRadius(), DELTA);
    }

    @Test
    void aThrowPublishesObjectThrown() {
        Pond pond = emptyPond();
        Level level = levelWithBag(pond, ThrowableKind.LOG);
        RecordingListener listener = new RecordingListener();
        level.addListener(listener);

        level.playTurn(throwAt(ThrowableKind.LOG, THROW_SPOT));

        assertEquals(1, listener.received.size());
        assertEquals(EventType.OBJECT_THROWN, listener.received.get(0).getType());
        assertEquals(ThrowableKind.LOG.size(), listener.received.get(0).getAmount(), DELTA);
    }

    @Test
    void aThrowWithNothingOfThatKindInTheBagIsRefused() {
        Pond pond = emptyPond();
        Level level = levelWithBag(pond, ThrowableKind.TWIG);
        RecordingListener listener = new RecordingListener();
        level.addListener(listener);

        level.playTurn(throwAt(ThrowableKind.LOG, THROW_SPOT));

        assertTrue(pond.elementsInOrder().isEmpty());
        assertTrue(pond.ripples().isEmpty());
        assertTrue(listener.received.isEmpty());
        assertTrue(level.getBag().has(ThrowableKind.TWIG));
    }

    @Test
    void aThrowOntoAnOccupiedSpotIsRefusedAndKeepsTheObject() {
        Pond pond = emptyPond();
        pond.add(new PlainElement(THROW_SPOT, 0.1));
        Level level = levelWithBag(pond, ThrowableKind.PLANK);

        level.playTurn(throwAt(ThrowableKind.PLANK, new Vec2(0.3, 0.25)));

        assertEquals(1, pond.elementsInOrder().size());
        assertTrue(level.getBag().has(ThrowableKind.PLANK));
    }

    @Test
    void aThrowOntoABankIsRefused() {
        Pond pond = new Pond(Pond.WIDTH, Pond.HEIGHT, 0.125, 0.125, new Frog(new Vec2(0.5, 0.625), Rank.RANK_1));
        Level level = levelWithBag(pond, ThrowableKind.PLANK);

        level.playTurn(throwAt(ThrowableKind.PLANK, new Vec2(0.5, 0.0625)));

        assertTrue(pond.elementsInOrder().isEmpty());
        assertTrue(level.getBag().has(ThrowableKind.PLANK));
    }

    @Test
    void aThrowOntoTheFrogIsRefused() {
        Pond pond = emptyPond();
        Level level = levelWithBag(pond, ThrowableKind.PLANK);

        level.playTurn(throwAt(ThrowableKind.PLANK, pond.getFrog().getPosition()));

        assertTrue(pond.elementsInOrder().isEmpty());
    }

    @Test
    void theFrogCanHopOntoWhatItThrewInTheSameTurn() {
        // The throw is step 1 and the move step 4, so the plank is already there.
        Frog frog = new Frog(new Vec2(0.5, 0.5625), Rank.RANK_1);
        Pond pond = new Pond(Pond.WIDTH, Pond.HEIGHT, 0.125, 0.125, frog);
        Level level = levelWithBag(pond, ThrowableKind.PLANK);
        Vec2 plankSpot = new Vec2(0.5, 0.4875);

        level.playTurn(new TurnAction(ThrowableKind.PLANK, plankSpot, new Hop(), plankSpot));

        assertEquals(plankSpot, frog.getPosition());
        assertTrue(frog.standingOn().isPresent());
    }

    // Frog moves (J6). The pond has banks 0.125 deep and the frog starts on the start
    // bank at (0.5, 0.625).

    private static final Vec2 FROG_START = new Vec2(0.5, 0.625);
    private static final Vec2 ALONG_THE_BANK = new Vec2(0.5625, 0.625);

    /** A pickup that records which frog collected it and then leaves the pond. */
    private static final class RecordingPickup extends Pickup {
        private Frog collectedBy;

        RecordingPickup(Vec2 position) {
            super(position, 0.03125);
        }

        @Override
        public void collect(Frog frog, Level level) {
            collectedBy = frog;
            level.getPond().remove(this);
        }
    }

    private static Pond pondWithBanks() {
        return new Pond(Pond.WIDTH, Pond.HEIGHT, 0.125, 0.125, new Frog(FROG_START, Rank.RANK_1));
    }

    private static TurnAction move(MoveMode mode, Vec2 target) {
        return new TurnAction(null, null, mode, target);
    }

    @Test
    void aLegalMoveMovesTheFrogAndPublishesFrogMoved() {
        Pond pond = pondWithBanks();
        Level level = levelOn(pond);
        RecordingListener listener = new RecordingListener();
        level.addListener(listener);

        level.playTurn(move(new Walk(), ALONG_THE_BANK));

        assertEquals(ALONG_THE_BANK, pond.getFrog().getPosition());
        assertEquals(1, listener.received.size());
        GameEvent event = listener.received.get(0);
        assertEquals(EventType.FROG_MOVED, event.getType());
        assertSame(pond.getFrog(), event.getSubject());
        assertEquals(0.0625, event.getAmount(), DELTA);
    }

    @Test
    void anIllegalMoveLeavesTheFrogWhereItIsAndPublishesNothing() {
        Pond pond = pondWithBanks();
        Level level = levelOn(pond);
        RecordingListener listener = new RecordingListener();
        level.addListener(listener);

        // A rank 1 frog cannot swim.
        level.playTurn(move(new Swim(), new Vec2(0.5, 0.53125)));

        assertEquals(FROG_START, pond.getFrog().getPosition());
        assertTrue(listener.received.isEmpty());
    }

    @Test
    void anUnaffordableMoveLeavesTheFrogWhereItIs() {
        Pond pond = pondWithBanks();
        pond.getFrog().spendEnergy(19.5);

        levelOn(pond).playTurn(move(new Walk(), new Vec2(0.5875, 0.625)));

        assertEquals(FROG_START, pond.getFrog().getPosition());
    }

    @Test
    void afterMovingTheFrogCollectsThePickupsItTouches() {
        Pond pond = pondWithBanks();
        RecordingPickup touched = new RecordingPickup(new Vec2(0.59375, 0.625));
        RecordingPickup farAway = new RecordingPickup(new Vec2(0.75, 0.625));
        pond.add(touched);
        pond.add(farAway);

        levelOn(pond).playTurn(move(new Walk(), ALONG_THE_BANK));

        assertSame(pond.getFrog(), touched.collectedBy);
        assertFalse(pond.elementsInOrder().contains(touched));
        assertNull(farAway.collectedBy);
        assertTrue(pond.elementsInOrder().contains(farAway));
    }

    @Test
    void aRefusedMoveCollectsNothing() {
        Pond pond = pondWithBanks();
        // Touching the frog where it stands, but the frog does not move.
        RecordingPickup underTheFrog = new RecordingPickup(FROG_START);
        pond.add(underTheFrog);

        levelOn(pond).playTurn(move(new Hop(), new Vec2(0.5, 0.53125)));

        assertNull(underTheFrog.collectedBy);
    }

    // TODO(J8) activation: pushes that move elements. Add once FloatingObject.push exists.
    // A floating object moves by the pushStep displacement and publishes OBJECT_PUSHED.
    //
    // TODO(S6) activation: predators. Add once Predator and its hook exist.
}