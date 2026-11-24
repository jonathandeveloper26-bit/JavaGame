package ripplepond.model.element;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import ripplepond.model.Balance;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.Pond;
import ripplepond.model.progress.Rank;

/**
 * Tests for {@link ThrownObject}: its size comes from its kind unless a level gives one,
 * and it floats and carries the frog like any {@link FloatingObject}.
 */
class ThrownObjectTest {

    private static final double DELTA = 1e-12;

    @Test
    void takesTheSizeOfItsKind() {
        for (ThrowableKind kind : ThrowableKind.values()) {
            ThrownObject object = new ThrownObject(new Vec2(0.5, 0.5), kind);

            assertSame(kind, object.getKind());
            assertEquals(kind.size(), object.getSize(), DELTA, kind.name());
        }
    }

    @Test
    void aLevelCanGiveItAnotherSize() {
        ThrownObject log = new ThrownObject(new Vec2(0.5, 0.5), ThrowableKind.LOG, 0.25);

        assertSame(ThrowableKind.LOG, log.getKind());
        assertEquals(0.25, log.getSize(), DELTA);
    }

    @Test
    void rejectsNullKindAndBadSize() {
        Vec2 centre = new Vec2(0.5, 0.5);

        assertThrows(NullPointerException.class, () -> new ThrownObject(centre, null));
        assertThrows(NullPointerException.class, () -> new ThrownObject(centre, null, 0.1));
        assertThrows(NullPointerException.class, () -> new ThrownObject(null, ThrowableKind.TWIG));
        assertThrows(IllegalArgumentException.class,
                () -> new ThrownObject(centre, ThrowableKind.TWIG, 0));
    }

    @Test
    void carriesTheFrogWhenPushed() {
        ThrownObject plank = new ThrownObject(new Vec2(0.5, 0.5), ThrowableKind.PLANK);
        Frog frog = new Frog(new Vec2(0.5, 0.5), Rank.RANK_1);
        Pond pond = new Pond(Balance.PLAY_AREA_WIDTH, Balance.PLAY_AREA_HEIGHT, frog);
        frog.standOn(plank);

        plank.push(new Vec2(1, 0), 0.25, pond);

        assertEquals(new Vec2(0.75, 0.5), frog.getPosition());
        assertSame(plank, frog.standingOn().orElseThrow());
    }
}
