package ripplepond.model.level;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for the two states a finished level can be in, {@link Won} and {@link Lost},
 * and for {@link LoseReason}. A finished level is over and refuses every turn.
 */
class FinishedStateTest {

    @Test
    void aWonLevelIsOver() {
        assertTrue(new Won().isOver());
    }

    @Test
    void aWonLevelRefusesATurn() {
        assertThrows(IllegalStateException.class, () -> new Won().playTurn(null, null));
    }

    @Test
    void aLostLevelIsOverAndKeepsItsReason() {
        for (LoseReason reason : LoseReason.values()) {
            Lost lost = new Lost(reason);

            assertTrue(lost.isOver(), reason.name());
            assertEquals(reason, lost.getReason());
        }
    }

    @Test
    void aLostLevelRefusesATurn() {
        Lost lost = new Lost(LoseReason.TURN_LIMIT);

        assertThrows(IllegalStateException.class, () -> lost.playTurn(null, null));
    }

    @Test
    void aLostLevelNeedsAReason() {
        assertThrows(NullPointerException.class, () -> new Lost(null));
    }

    @Test
    void thereAreExactlyThreeWaysToLose() {
        // An empty throw bag is deliberately not one of them (GDD 3.2).
        assertArrayEquals(
                new LoseReason[] {LoseReason.NO_HEALTH, LoseReason.NO_ENERGY, LoseReason.TURN_LIMIT},
                LoseReason.values());
    }

    @Test
    void statesDescribeThemselves() {
        assertEquals("Won", new Won().toString());
        assertEquals("Lost(NO_ENERGY)", new Lost(LoseReason.NO_ENERGY).toString());
    }
}
