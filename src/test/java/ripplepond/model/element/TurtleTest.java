package ripplepond.model.element;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import ripplepond.model.event.EventType;
import ripplepond.model.event.GameEvent;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.InProgress;
import ripplepond.model.level.Level;
import ripplepond.model.level.Pond;
import ripplepond.model.level.ThrowBag;
import ripplepond.model.progress.Rank;

/**
 * Tests for {@link Turtle}: it starts asleep, a ripple wakes it and moves it by the
 * push, and the same things that block a floating object block it. The turtle is at
 * (0.5, 0.25) with radius 0.0625; the pond is all water.
 */
class TurtleTest {

    private static final double DELTA = 1e-12;
    private static final Vec2 START = new Vec2(0.5, 0.25);
    private static final Vec2 PUSH_RIGHT = new Vec2(0.0625, 0);

    private final Turtle turtle = new Turtle(START, 0.125);
    private final Pond pond = new Pond(Pond.WIDTH, Pond.HEIGHT, new Frog(new Vec2(0, 0), Rank.RANK_1));
    private final Level level = new Level(pond, new ThrowBag(), 40, new InProgress());
    private final List<GameEvent> events = new ArrayList<>();

    TurtleTest() {
        pond.add(turtle);
        level.addListener(events::add);
    }

    @Test
    void startsAsleep() {
        assertFalse(turtle.isAwake());
    }

    @Test
    void hasTheSizeAndPositionItIsGiven() {
        assertEquals(START, turtle.getPosition());
        assertEquals(0.125, turtle.getSize(), DELTA);
    }

    @Test
    void rejectsBadArguments() {
        assertThrows(NullPointerException.class, () -> new Turtle(null, 0.125));
        assertThrows(IllegalArgumentException.class, () -> new Turtle(new Vec2(0.5, 0.5), 0));
    }

    @Test
    void aTurtleIsSolid() {
        assertTrue(turtle.isSolid());
    }

    @Test
    void aRippleWakesTheTurtleAndMovesItAwayFromTheSource() {
        turtle.reactTo(PUSH_RIGHT, level);

        assertTrue(turtle.isAwake());
        assertEquals(new Vec2(0.5625, 0.25), turtle.getPosition());
    }

    @Test
    void wakingPublishesTurtleWokeWithTheDistanceMoved() {
        turtle.reactTo(PUSH_RIGHT, level);

        assertEquals(1, events.size());
        assertEquals(EventType.TURTLE_WOKE, events.get(0).getType());
        assertSame(turtle, events.get(0).getSubject());
        assertEquals(0.0625, events.get(0).getAmount(), DELTA);
    }

    @Test
    void anAwakeTurtleMovesAgainButWakesOnlyOnce() {
        turtle.reactTo(PUSH_RIGHT, level);
        turtle.reactTo(PUSH_RIGHT, level);

        assertEquals(new Vec2(0.625, 0.25), turtle.getPosition());
        assertEquals(1, events.size());
    }

    @Test
    void aReedInTheWayWakesTheTurtleButDoesNotMoveIt() {
        // The turtle's right edge would reach 0.625; the reed starts at 0.59375.
        pond.add(new Reed(new Vec2(0.65625, 0.25), 0.125));

        turtle.reactTo(PUSH_RIGHT, level);

        assertTrue(turtle.isAwake());
        assertEquals(START, turtle.getPosition());
        assertEquals(0, events.get(0).getAmount(), DELTA);
    }

    @Test
    void aFloatingObjectInTheWayBlocksTheTurtle() {
        pond.add(new LilyPad(new Vec2(0.65625, 0.25), 0.125));

        turtle.reactTo(PUSH_RIGHT, level);

        assertEquals(START, turtle.getPosition());
    }

    @Test
    void aBankInTheWayBlocksTheTurtle() {
        Pond withBanks = new Pond(Pond.WIDTH, Pond.HEIGHT, 0.1875, 0.125, new Frog(new Vec2(0, 0), Rank.RANK_1));
        withBanks.add(turtle);
        Level levelWithBanks = new Level(withBanks, new ThrowBag(), 40, new InProgress());

        // Pushed up, the turtle's top edge would go from 0.1875 into the far bank.
        turtle.reactTo(new Vec2(0, -0.0625), levelWithBanks);

        assertEquals(START, turtle.getPosition());
    }

    @Test
    void ripplesThatCancelOutWakeTheTurtleWithoutMovingIt() {
        turtle.reactTo(new Vec2(0, 0), level);

        assertTrue(turtle.isAwake());
        assertEquals(START, turtle.getPosition());
        assertEquals(EventType.TURTLE_WOKE, events.get(0).getType());
    }

    @Test
    void aMovedTurtleBlocksAFloatingObject() {
        turtle.reactTo(PUSH_RIGHT, level);
        LilyPad pad = new LilyPad(new Vec2(0.75, 0.25), 0.125);
        pond.add(pad);

        // The turtle's right edge is now at 0.625. The pad's left edge, at 0.6875, would
        // move to 0.5625: into the turtle.
        assertFalse(pad.push(new Vec2(-1, 0), 0.125, pond));
    }
}
