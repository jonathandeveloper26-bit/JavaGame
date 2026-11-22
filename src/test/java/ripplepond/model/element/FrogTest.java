package ripplepond.model.element;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ripplepond.model.Balance;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.progress.Rank;

/**
 * Tests for {@link Frog}: health and energy start full for the rank and always stay
 * between 0 and the maximum. A rank 1 frog has 10 HP and 20 energy at most.
 */
class FrogTest {

    private static final double DELTA = 1e-12;

    private final Frog frog = new Frog(new Vec2(0.5, 0.5), Rank.RANK_1);

    // Creating a frog

    @Test
    void startsWithFullHealthAndEnergyForItsRank() {
        Frog rankThree = new Frog(new Vec2(0.5, 0.5), Rank.RANK_3);

        assertEquals(10, frog.getHealth());
        assertEquals(20, frog.getEnergy(), DELTA);
        assertEquals(14, rankThree.getHealth());
        assertEquals(28, rankThree.getEnergy(), DELTA);
    }

    @Test
    void hasTheFrogSizeAndItsRank() {
        assertEquals(Balance.FROG_SIZE, frog.getSize(), DELTA);
        assertEquals(Rank.RANK_1, frog.getRank());
    }

    @Test
    void startsAliveWithEnergyAndNotOnAnObject() {
        assertTrue(frog.isAlive());
        assertTrue(frog.hasEnergy());
        assertTrue(frog.standingOn().isEmpty());
    }

    @Test
    void constructorRejectsNullRankOrPosition() {
        assertThrows(NullPointerException.class, () -> new Frog(new Vec2(0, 0), null));
        assertThrows(NullPointerException.class, () -> new Frog(null, Rank.RANK_1));
    }

    // Health

    @Test
    void takeDamageLowersHealth() {
        frog.takeDamage(3);

        assertEquals(7, frog.getHealth());
        assertTrue(frog.isAlive());
    }

    @Test
    void healthNeverDropsBelowZero() {
        frog.takeDamage(25);

        assertEquals(0, frog.getHealth());
        assertFalse(frog.isAlive());
    }

    @Test
    void damageExactlyEqualToHealthLeavesTheFrogDead() {
        frog.takeDamage(10);

        assertEquals(0, frog.getHealth());
        assertFalse(frog.isAlive());
    }

    @Test
    void restoreHealthRaisesHealth() {
        frog.takeDamage(5);
        frog.restoreHealth(2);

        assertEquals(7, frog.getHealth());
    }

    @Test
    void healthNeverRisesAboveTheMaximum() {
        frog.takeDamage(1);
        frog.restoreHealth(5);

        assertEquals(10, frog.getHealth());
    }

    @Test
    void aHugeRestoreDoesNotOverflow() {
        frog.restoreHealth(Integer.MAX_VALUE);

        assertEquals(10, frog.getHealth());
    }

    @Test
    void zeroDamageOrHealingChangesNothing() {
        frog.takeDamage(0);
        frog.restoreHealth(0);

        assertEquals(10, frog.getHealth());
    }

    @Test
    void healthMethodsRejectNegativeAmounts() {
        assertThrows(IllegalArgumentException.class, () -> frog.takeDamage(-1));
        assertThrows(IllegalArgumentException.class, () -> frog.restoreHealth(-1));
    }

    // Energy

    @Test
    void spendEnergyLowersEnergy() {
        frog.spendEnergy(2.5);

        assertEquals(17.5, frog.getEnergy(), DELTA);
        assertTrue(frog.hasEnergy());
    }

    @Test
    void energyNeverDropsBelowZero() {
        frog.spendEnergy(30);

        assertEquals(0, frog.getEnergy(), DELTA);
        assertFalse(frog.hasEnergy());
    }

    @Test
    void restoreEnergyRaisesEnergy() {
        frog.spendEnergy(10);
        frog.restoreEnergy(4);

        assertEquals(14, frog.getEnergy(), DELTA);
    }

    @Test
    void energyNeverRisesAboveTheMaximum() {
        frog.spendEnergy(1);
        frog.restoreEnergy(5);

        assertEquals(20, frog.getEnergy(), DELTA);
    }

    @Test
    void energyMethodsRejectNegativeOrNaNAmounts() {
        assertThrows(IllegalArgumentException.class, () -> frog.spendEnergy(-0.5));
        assertThrows(IllegalArgumentException.class, () -> frog.spendEnergy(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> frog.restoreEnergy(-0.5));
        assertThrows(IllegalArgumentException.class, () -> frog.restoreEnergy(Double.NaN));
    }

    @Test
    void aRejectedAmountChangesNothing() {
        assertThrows(IllegalArgumentException.class, () -> frog.takeDamage(-1));
        assertThrows(IllegalArgumentException.class, () -> frog.spendEnergy(-1));

        assertEquals(10, frog.getHealth());
        assertEquals(20, frog.getEnergy(), DELTA);
    }
}
