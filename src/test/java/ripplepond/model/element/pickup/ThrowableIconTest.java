package ripplepond.model.element.pickup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import ripplepond.model.element.Frog;
import ripplepond.model.element.ThrowableKind;
import ripplepond.model.event.EventType;
import ripplepond.model.event.GameEvent;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.InProgress;
import ripplepond.model.level.Level;
import ripplepond.model.level.Pond;
import ripplepond.model.level.ThrowBag;
import ripplepond.model.level.TurnAction;
import ripplepond.model.movement.Walk;
import ripplepond.model.progress.Rank;

/**
 * Tests for {@link ThrowableIcon}: touching it puts its object in the throw bag, removes
 * it from the pond and publishes {@code ICON_GATHERED}.
 */
class ThrowableIconTest {

    private static final double DELTA = 1e-12;

    private final Frog frog = new Frog(new Vec2(0.5, 0.625), Rank.RANK_1);
    private final Pond pond = new Pond(Pond.WIDTH, Pond.HEIGHT, 0.125, 0.125, frog);
    private final ThrowBag bag = new ThrowBag();
    private final Level level = new Level(pond, bag, 40, new InProgress());
    private final List<GameEvent> events = new ArrayList<>();

    ThrowableIconTest() {
        level.addListener(events::add);
    }

    @Test
    void anIconHasItsKindAndTheUsualSize() {
        ThrowableIcon icon = new ThrowableIcon(new Vec2(0.25, 0.5), ThrowableKind.LOG);

        assertEquals(ThrowableKind.LOG, icon.getKind());
        assertEquals(0.04, icon.getSize(), DELTA);
        assertFalse(icon.isSolid());
    }

    @Test
    void collectingPutsOneObjectOfItsKindInTheBag() {
        ThrowableIcon icon = new ThrowableIcon(new Vec2(0.5, 0.625), ThrowableKind.PLANK);
        pond.add(icon);

        icon.collect(frog, level);

        assertEquals(1, bag.count(ThrowableKind.PLANK));
        assertTrue(bag.has(ThrowableKind.PLANK));
    }

    @Test
    void collectingRemovesTheIconAndPublishesIconGathered() {
        ThrowableIcon icon = new ThrowableIcon(new Vec2(0.5, 0.625), ThrowableKind.TWIG);
        pond.add(icon);

        icon.collect(frog, level);

        assertFalse(pond.elementsInOrder().contains(icon));
        assertEquals(1, events.size());
        assertEquals(EventType.ICON_GATHERED, events.get(0).getType());
        assertSame(icon, events.get(0).getSubject());
        assertEquals(1, events.get(0).getAmount(), DELTA);
    }

    @Test
    void walkingOntoAnIconGathersIt() {
        // The icon is 0.0625 along the start bank; the frog walks right onto it.
        ThrowableIcon icon = new ThrowableIcon(new Vec2(0.5625, 0.625), ThrowableKind.LOG);
        pond.add(icon);

        level.playTurn(new TurnAction(null, null, new Walk(), new Vec2(0.5625, 0.625)));

        assertEquals(1, bag.count(ThrowableKind.LOG));
        assertFalse(pond.elementsInOrder().contains(icon));
        assertEquals(List.of(EventType.FROG_MOVED, EventType.ICON_GATHERED),
                events.stream().map(GameEvent::getType).toList());
    }

    @Test
    void rejectsBadArguments() {
        Vec2 at = new Vec2(0.5, 0.5);
        ThrowableIcon icon = new ThrowableIcon(at, ThrowableKind.TWIG);

        assertThrows(NullPointerException.class, () -> new ThrowableIcon(at, null));
        assertThrows(NullPointerException.class, () -> new ThrowableIcon(null, ThrowableKind.TWIG));
        assertThrows(IllegalArgumentException.class, () -> new ThrowableIcon(at, ThrowableKind.TWIG, 0));
        assertThrows(NullPointerException.class, () -> icon.collect(null, level));
        assertThrows(NullPointerException.class, () -> icon.collect(frog, null));
    }
}
