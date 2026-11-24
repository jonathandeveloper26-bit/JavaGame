package ripplepond.model.element;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.Pond;
import ripplepond.model.movement.Hop;
import ripplepond.model.movement.MoveMode;
import ripplepond.model.movement.Swim;
import ripplepond.model.movement.Walk;
import ripplepond.model.progress.Rank;

/**
 * Tests for {@link Frog#move} and {@link Frog#canAfford}: a move is paid for, the frog
 * ends where it was sent and stands on what is there, and a refused move changes
 * nothing.
 * <p>
 * The pond has banks 0.125 deep; the frog starts on the start bank at (0.5, 0.625).
 */
class FrogMoveTest {

    private static final double DELTA = 1e-12;
    private static final Vec2 START = new Vec2(0.5, 0.625);
    private static final Vec2 IN_WATER_NEAR_BANK = new Vec2(0.5, 0.53125);

    private final MoveMode walk = new Walk();
    private final MoveMode hop = new Hop();
    private final MoveMode swim = new Swim();

    private static Pond pondFor(Frog frog) {
        return new Pond(Pond.WIDTH, Pond.HEIGHT, 0.125, 0.125, frog);
    }

    private static LilyPad padAt(Pond pond, Vec2 centre) {
        LilyPad pad = new LilyPad(centre, 0.125);
        pond.add(pad);
        return pad;
    }

    // Moves that happen

    @Test
    void aWalkMovesTheFrogAndCostsOnePerTenth() {
        Frog frog = new Frog(START, Rank.RANK_1);

        assertTrue(frog.move(walk, new Vec2(0.5625, 0.625), pondFor(frog)));

        assertEquals(new Vec2(0.5625, 0.625), frog.getPosition());
        assertEquals(20 - 0.625, frog.getEnergy(), DELTA);
        assertTrue(frog.standingOn().isEmpty());
    }

    @Test
    void aHopOntoAPadLeavesTheFrogStandingOnIt() {
        Frog frog = new Frog(START, Rank.RANK_1);
        Pond pond = pondFor(frog);
        LilyPad pad = padAt(pond, IN_WATER_NEAR_BANK);

        assertTrue(frog.move(hop, IN_WATER_NEAR_BANK, pond));

        assertSame(pad, frog.standingOn().orElseThrow());
        assertSame(frog, pad.getRider().orElseThrow());
        assertEquals(20 - 1.875, frog.getEnergy(), DELTA);
    }

    @Test
    void aHopFromAPadOntoTheBankLeavesThePad() {
        Frog frog = new Frog(new Vec2(0.5, 0.1875), Rank.RANK_1);
        Pond pond = pondFor(frog);
        LilyPad pad = padAt(pond, frog.getPosition());
        frog.standOn(pad);

        assertTrue(frog.move(hop, new Vec2(0.5, 0.09375), pond));

        assertTrue(frog.standingOn().isEmpty());
        assertTrue(pad.getRider().isEmpty());
    }

    @Test
    void aWalkOnAPadKeepsTheFrogOnIt() {
        Frog frog = new Frog(new Vec2(0.5, 0.375), Rank.RANK_1);
        Pond pond = pondFor(frog);
        LilyPad pad = padAt(pond, frog.getPosition());
        frog.standOn(pad);

        assertTrue(frog.move(walk, new Vec2(0.53125, 0.375), pond));

        assertSame(pad, frog.standingOn().orElseThrow());
        assertSame(frog, pad.getRider().orElseThrow());
    }

    @Test
    void aSwimIntoTheWaterAndOntoAPad() {
        Frog frog = new Frog(START, Rank.RANK_3);
        Pond pond = pondFor(frog);
        LilyPad pad = padAt(pond, new Vec2(0.5, 0.4375));

        assertTrue(frog.move(swim, IN_WATER_NEAR_BANK, pond));
        assertTrue(frog.standingOn().isEmpty());
        assertEquals(28 - 3.75, frog.getEnergy(), DELTA);

        assertTrue(frog.move(swim, new Vec2(0.5, 0.4375), pond));
        assertSame(pad, frog.standingOn().orElseThrow());
    }

    // Refused moves

    @Test
    void anIllegalMoveIsRefusedAndChangesNothing() {
        Frog frog = new Frog(START, Rank.RANK_1);

        // Hopping into open water is not allowed.
        assertFalse(frog.move(hop, IN_WATER_NEAR_BANK, pondFor(frog)));

        assertEquals(START, frog.getPosition());
        assertEquals(20, frog.getEnergy(), DELTA);
    }

    @Test
    void aMoveTheFrogCannotPayForIsRefusedAndChangesNothing() {
        Frog frog = new Frog(START, Rank.RANK_1);
        Pond pond = pondFor(frog);
        LilyPad pad = padAt(pond, IN_WATER_NEAR_BANK);
        frog.spendEnergy(19);

        // The hop costs 1.875; the frog has 1.
        assertFalse(frog.canAfford(hop, IN_WATER_NEAR_BANK));
        assertFalse(frog.move(hop, IN_WATER_NEAR_BANK, pond));

        assertEquals(START, frog.getPosition());
        assertEquals(1, frog.getEnergy(), DELTA);
        assertTrue(pad.getRider().isEmpty());
    }

    @Test
    void exactlyEnoughEnergyPaysForTheMoveAndLeavesNone() {
        Frog frog = new Frog(START, Rank.RANK_1);
        frog.spendEnergy(20 - 0.625);

        assertTrue(frog.canAfford(walk, new Vec2(0.5625, 0.625)));
        assertTrue(frog.move(walk, new Vec2(0.5625, 0.625), pondFor(frog)));

        assertEquals(0, frog.getEnergy());
        assertFalse(frog.hasEnergy());
    }

    @Test
    void slightlyTooLittleEnergyIsRefused() {
        Frog frog = new Frog(START, Rank.RANK_1);
        frog.spendEnergy(20 - 0.5);

        assertFalse(frog.move(walk, new Vec2(0.5625, 0.625), pondFor(frog)));
        assertEquals(0.5, frog.getEnergy(), DELTA);
    }

    @Test
    void aFreshFrogCanAffordAnyMoveWithinItsRange() {
        Frog frog = new Frog(START, Rank.RANK_1);

        assertTrue(frog.canAfford(walk, new Vec2(0.6, 0.625)));
        assertTrue(frog.canAfford(hop, new Vec2(0.5, 0.525)));
    }

    @Test
    void rejectsNullArguments() {
        Frog frog = new Frog(START, Rank.RANK_1);
        Pond pond = pondFor(frog);
        Vec2 target = new Vec2(0.5625, 0.625);

        assertThrows(NullPointerException.class, () -> frog.move(null, target, pond));
        assertThrows(NullPointerException.class, () -> frog.move(walk, null, pond));
        assertThrows(NullPointerException.class, () -> frog.move(walk, target, null));
        assertThrows(NullPointerException.class, () -> frog.canAfford(null, target));
        assertThrows(NullPointerException.class, () -> frog.canAfford(walk, null));
    }
}
