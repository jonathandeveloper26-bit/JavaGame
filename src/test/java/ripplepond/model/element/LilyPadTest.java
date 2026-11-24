package ripplepond.model.element;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ripplepond.model.Balance;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.Pond;
import ripplepond.model.progress.Rank;

/**
 * Tests for {@link LilyPad}: it has the size it is given and floats and carries the frog
 * like any {@link FloatingObject}.
 */
class LilyPadTest {

    private static final double DELTA = 1e-12;

    @Test
    void hasTheSizeItIsGiven() {
        LilyPad pad = new LilyPad(new Vec2(0.5, 0.5), 0.125);

        assertEquals(0.125, pad.getSize(), DELTA);
        assertTrue(pad.covers(new Vec2(0.5625, 0.5)));
    }

    @Test
    void rejectsBadArguments() {
        assertThrows(NullPointerException.class, () -> new LilyPad(null, 0.125));
        assertThrows(IllegalArgumentException.class, () -> new LilyPad(new Vec2(0.5, 0.5), -1));
    }

    @Test
    void carriesTheFrogWhenPushed() {
        LilyPad pad = new LilyPad(new Vec2(0.5, 0.5), 0.125);
        Frog frog = new Frog(new Vec2(0.5, 0.5), Rank.RANK_1);
        Pond pond = new Pond(Balance.PLAY_AREA_WIDTH, Balance.PLAY_AREA_HEIGHT, frog);
        frog.standOn(pad);

        pad.push(new Vec2(0, -1), 0.25, pond);

        assertEquals(new Vec2(0.5, 0.25), frog.getPosition());
        assertSame(pad, frog.standingOn().orElseThrow());
    }
}
