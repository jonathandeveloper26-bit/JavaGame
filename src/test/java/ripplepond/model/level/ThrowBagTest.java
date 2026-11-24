package ripplepond.model.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ripplepond.model.element.ThrowableKind;
import ripplepond.model.element.ThrownObject;
import ripplepond.model.geometry.Vec2;

/**
 * Tests for {@link ThrowBag}: adding, counting, taking, and refusing to take what is not
 * there.
 */
class ThrowBagTest {

    private static final double DELTA = 1e-12;
    private static final Vec2 TARGET = new Vec2(0.5, 0.25);

    private final ThrowBag bag = new ThrowBag();

    @Test
    void aNewBagIsEmpty() {
        assertTrue(bag.isEmpty());
        for (ThrowableKind kind : ThrowableKind.values()) {
            assertEquals(0, bag.count(kind), kind.name());
            assertFalse(bag.has(kind), kind.name());
        }
    }

    @Test
    void addPutsOneObjectInTheBag() {
        bag.add(ThrowableKind.PLANK);

        assertFalse(bag.isEmpty());
        assertTrue(bag.has(ThrowableKind.PLANK));
        assertEquals(1, bag.count(ThrowableKind.PLANK));
    }

    @Test
    void eachKindIsCountedSeparately() {
        bag.add(ThrowableKind.TWIG);
        bag.add(ThrowableKind.TWIG);
        bag.add(ThrowableKind.LOG);

        assertEquals(2, bag.count(ThrowableKind.TWIG));
        assertEquals(0, bag.count(ThrowableKind.PLANK));
        assertEquals(1, bag.count(ThrowableKind.LOG));
        assertFalse(bag.has(ThrowableKind.PLANK));
    }

    @Test
    void takeReturnsAnObjectOfThatKindAtTheTarget() {
        bag.add(ThrowableKind.LOG);

        ThrownObject log = bag.take(ThrowableKind.LOG, TARGET);

        assertSame(ThrowableKind.LOG, log.getKind());
        assertEquals(TARGET, log.getPosition());
        assertEquals(ThrowableKind.LOG.size(), log.getSize(), DELTA);
    }

    @Test
    void takeRemovesOneObject() {
        bag.add(ThrowableKind.TWIG);
        bag.add(ThrowableKind.TWIG);

        bag.take(ThrowableKind.TWIG, TARGET);

        assertEquals(1, bag.count(ThrowableKind.TWIG));
        assertTrue(bag.has(ThrowableKind.TWIG));
    }

    @Test
    void takingTheLastObjectEmptiesTheBag() {
        bag.add(ThrowableKind.TWIG);

        bag.take(ThrowableKind.TWIG, TARGET);

        assertEquals(0, bag.count(ThrowableKind.TWIG));
        assertFalse(bag.has(ThrowableKind.TWIG));
        assertTrue(bag.isEmpty());
    }

    @Test
    void eachTakeCreatesANewObject() {
        bag.add(ThrowableKind.PLANK);
        bag.add(ThrowableKind.PLANK);

        ThrownObject first = bag.take(ThrowableKind.PLANK, TARGET);
        ThrownObject second = bag.take(ThrowableKind.PLANK, new Vec2(0.25, 0.25));

        assertFalse(first == second);
        assertEquals(TARGET, first.getPosition());
    }

    @Test
    void takingFromAnEmptyBagIsRefused() {
        assertThrows(IllegalStateException.class, () -> bag.take(ThrowableKind.TWIG, TARGET));
        assertTrue(bag.isEmpty());
    }

    @Test
    void takingAKindTheBagDoesNotHoldIsRefusedAndChangesNothing() {
        bag.add(ThrowableKind.TWIG);

        assertThrows(IllegalStateException.class, () -> bag.take(ThrowableKind.LOG, TARGET));
        assertEquals(1, bag.count(ThrowableKind.TWIG));
        assertEquals(0, bag.count(ThrowableKind.LOG));
    }

    @Test
    void nullArgumentsAreRefusedAndChangeNothing() {
        bag.add(ThrowableKind.TWIG);

        assertThrows(NullPointerException.class, () -> bag.add(null));
        assertThrows(NullPointerException.class, () -> bag.count(null));
        assertThrows(NullPointerException.class, () -> bag.has(null));
        assertThrows(NullPointerException.class, () -> bag.take(null, TARGET));
        assertThrows(NullPointerException.class, () -> bag.take(ThrowableKind.TWIG, null));
        assertEquals(1, bag.count(ThrowableKind.TWIG));
    }
}
