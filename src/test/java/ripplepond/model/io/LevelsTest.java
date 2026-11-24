package ripplepond.model.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import ripplepond.model.element.Element;
import ripplepond.model.element.Frog;
import ripplepond.model.element.LilyPad;
import ripplepond.model.element.ThrowableKind;
import ripplepond.model.element.Turtle;
import ripplepond.model.element.pickup.Breadcrumb;
import ripplepond.model.element.pickup.Fly;
import ripplepond.model.element.pickup.Food;
import ripplepond.model.element.pickup.Minnow;
import ripplepond.model.element.pickup.ThrowableIcon;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.InProgress;
import ripplepond.model.level.Level;
import ripplepond.model.level.TurnAction;
import ripplepond.model.level.Won;
import ripplepond.model.movement.Hop;
import ripplepond.model.movement.MoveMode;
import ripplepond.model.movement.Swim;
import ripplepond.model.movement.Walk;
import ripplepond.model.progress.Rank;

/**
 * Plays the game's real level files from start to finish, with a rank 1 frog and only
 * the level's starting bag. Until the level screen exists, these tests stand in for
 * "win every level yourself before merging it" (LEVEL_FORMAT.md section 5).
 */
class LevelsTest {

    private final LevelLoader loader = new LevelLoader();

    private static TurnAction throwAndMove(ThrowableKind kind, Vec2 throwAt, MoveMode mode, Vec2 moveTo) {
        return new TurnAction(kind, throwAt, mode, moveTo);
    }

    private static TurnAction move(MoveMode mode, Vec2 moveTo) {
        return new TurnAction(null, null, mode, moveTo);
    }

    /** Plays one turn and checks the frog ended where it was sent. */
    private static void play(Level level, TurnAction action, Vec2 frogShouldBeAt) {
        level.playTurn(action);
        Vec2 frog = level.getPond().getFrog().getPosition();
        assertEquals(frogShouldBeAt.getX(), frog.getX(), 1e-9, "turn " + level.getTurn() + " x");
        assertEquals(frogShouldBeAt.getY(), frog.getY(), 1e-9, "turn " + level.getTurn() + " y");
    }

    // Level 1

    @Test
    void levelOneLoads() throws LevelFormatException {
        Level level = loader.load("levels/level1.txt", Rank.RANK_1);

        assertEquals(25, level.getTurnLimit());
        assertEquals(6, level.getBag().count(ThrowableKind.TWIG));
        assertEquals(2, level.getBag().count(ThrowableKind.PLANK));
        assertInstanceOf(InProgress.class, level.getState());
    }

    @Test
    void levelOneCannotBeCrossedWithoutThrowing() throws LevelFormatException {
        Level level = loader.load("levels/level1.txt", Rank.RANK_1);
        Frog frog = level.getPond().getFrog();

        // From the water's edge, there is nothing to hop onto.
        level.playTurn(move(new Walk(), new Vec2(0.50, 0.56)));
        level.playTurn(move(new Hop(), new Vec2(0.50, 0.47)));

        assertEquals(new Vec2(0.50, 0.56), frog.getPosition());
    }

    @Test
    void levelOneCanBeWonWithItsStartingBag() throws LevelFormatException {
        Level level = loader.load("levels/level1.txt", Rank.RANK_1);
        Hop hop = new Hop();
        Vec2 twig1 = new Vec2(0.50, 0.47);
        Vec2 twig2 = new Vec2(0.50, 0.38);
        Vec2 twig3 = new Vec2(0.50, 0.29);
        Vec2 twig4 = new Vec2(0.50, 0.20);
        Vec2 edge = new Vec2(0.50, 0.56);
        Vec2 farBank = new Vec2(0.50, 0.11);

        // Throw the first twig and walk to the water's edge.
        play(level, throwAndMove(ThrowableKind.TWIG, twig1, new Walk(), edge), edge);
        // Each turn: throw the next twig ahead, hop onto the one before.
        play(level, throwAndMove(ThrowableKind.TWIG, twig2, hop, twig1), twig1);
        play(level, throwAndMove(ThrowableKind.TWIG, twig3, hop, twig2), twig2);
        play(level, throwAndMove(ThrowableKind.TWIG, twig4, hop, twig3), twig3);
        play(level, move(hop, twig4), twig4);
        play(level, move(hop, farBank), farBank);

        assertInstanceOf(Won.class, level.getState());
        assertEquals(6, level.getTurn());
        Frog frog = level.getPond().getFrog();
        assertTrue(frog.getEnergy() > 10, "energy left: " + frog.getEnergy());
        assertEquals(10, frog.getHealth());
        assertEquals(2, level.getBag().count(ThrowableKind.TWIG));
        assertFalse(level.getBag().isEmpty());
    }

    // Level 2

    private static final Vec2 PAD_A = new Vec2(0.50, 0.47);
    private static final Vec2 PAD_B_START = new Vec2(0.50, 0.30);
    private static final Vec2 PAD_B_PUSHED = new Vec2(0.50, 0.38);

    private static Element elementAt(Level level, Class<? extends Element> type, int index) {
        return level.getPond().elementsInOrder().stream().filter(type::isInstance).toList().get(index);
    }

    @Test
    void levelTwoLoadsWithPadsATurtleAnIconAndAFly() throws LevelFormatException {
        Level level = loader.load("levels/level2.txt", Rank.RANK_1);

        assertEquals(20, level.getTurnLimit());
        assertEquals(3, level.getBag().count(ThrowableKind.TWIG));
        assertEquals(2, level.getPond().elementsInOrder().stream().filter(LilyPad.class::isInstance).count());
        assertEquals(1, level.getPond().elementsInOrder().stream().filter(Turtle.class::isInstance).count());
        assertEquals(1, level.getPond().elementsInOrder().stream().filter(ThrowableIcon.class::isInstance).count());
        assertEquals(1, level.getPond().elementsInOrder().stream().filter(Fly.class::isInstance).count());
    }

    @Test
    void levelTwoPadBIsOutOfReachUntilARipplePushesIt() throws LevelFormatException {
        Level level = loader.load("levels/level2.txt", Rank.RANK_1);
        Frog frog = level.getPond().getFrog();
        play(level, move(new Walk(), new Vec2(0.50, 0.56)), new Vec2(0.50, 0.56));
        play(level, move(new Hop(), PAD_A), PAD_A);

        level.playTurn(move(new Hop(), PAD_B_START));

        assertEquals(PAD_A, frog.getPosition());
    }

    @Test
    void levelTwoCanBeWonWithItsStartingBag() throws LevelFormatException {
        Level level = loader.load("levels/level2.txt", Rank.RANK_1);
        Hop hop = new Hop();
        Vec2 twig1 = new Vec2(0.50, 0.215);
        Vec2 twig2 = new Vec2(0.50, 0.295);
        Vec2 edge = new Vec2(0.50, 0.56);
        Vec2 farBank = new Vec2(0.50, 0.118);
        Turtle turtle = (Turtle) elementAt(level, Turtle.class, 0);
        LilyPad padB = (LilyPad) elementAt(level, LilyPad.class, 0);
        assertEquals(PAD_B_START, padB.getPosition());

        // Throw beyond pad B: its ripple pushes B towards A. Walk to the edge.
        play(level, throwAndMove(ThrowableKind.TWIG, twig1, new Walk(), edge), edge);
        assertEquals(PAD_B_PUSHED.getY(), padB.getPosition().getY(), 1e-9);

        // Hop onto A, picking up the plank icon on it. The ripple wakes the turtle.
        play(level, move(hop, PAD_A), PAD_A);
        assertEquals(1, level.getBag().count(ThrowableKind.PLANK));
        assertTrue(turtle.isAwake());

        // Throw the second twig between B and the first one, then hop along.
        play(level, throwAndMove(ThrowableKind.TWIG, twig2, hop, PAD_B_PUSHED), PAD_B_PUSHED);
        play(level, move(hop, twig2), twig2);
        play(level, move(hop, twig1), twig1);
        play(level, move(hop, farBank), farBank);

        assertInstanceOf(Won.class, level.getState());
        assertEquals(6, level.getTurn());
        Frog frog = level.getPond().getFrog();
        assertTrue(frog.getEnergy() > 10, "energy left: " + frog.getEnergy());
        assertEquals(1, level.getBag().count(ThrowableKind.TWIG));
    }

    // Level 5, played with a rank 3 frog (hop 0.14, can swim)

    private static final Vec2 L5_EDGE = new Vec2(0.50, 0.595);
    private static final Vec2 L5_PAD_1 = new Vec2(0.50, 0.46);
    private static final Vec2 L5_PAD_2 = new Vec2(0.46, 0.27);

    private static LilyPad padTwo(Level level) {
        return level.getPond().elementsInOrder().stream()
                .filter(LilyPad.class::isInstance).map(LilyPad.class::cast)
                .filter(pad -> pad.getPosition().getY() < 0.40).findFirst().orElseThrow();
    }

    @Test
    void levelFiveLoadsWithOneFoodOfEachKind() throws LevelFormatException {
        Level level = loader.load("levels/level5.txt", Rank.RANK_3);

        assertEquals(15, level.getTurnLimit());
        assertEquals(2, level.getBag().count(ThrowableKind.TWIG));
        for (Class<?> food : List.of(Fly.class, Minnow.class, Breadcrumb.class)) {
            assertEquals(1, level.getPond().elementsInOrder().stream().filter(food::isInstance).count(),
                    food.getSimpleName());
        }
    }

    @Test
    void levelFiveTheReedShadowsPadTwoFromStraightAbove() throws LevelFormatException {
        Level level = loader.load("levels/level5.txt", Rank.RANK_3);
        LilyPad padTwo = padTwo(level);

        // The obvious throw, straight above pad 2: the reed blocks its ripple.
        level.playTurn(new TurnAction(ThrowableKind.TWIG, new Vec2(0.46, 0.14), null, null));
        level.playTurn(move(new Walk(), L5_EDGE));
        level.playTurn(move(new Hop(), L5_PAD_1));

        assertEquals(L5_PAD_2, padTwo.getPosition());
        level.playTurn(move(new Hop(), L5_PAD_2));
        assertEquals(L5_PAD_1, level.getPond().getFrog().getPosition());
    }

    @Test
    void levelFiveCanBeWonWithItsStartingBag() throws LevelFormatException {
        Level level = loader.load("levels/level5.txt", Rank.RANK_3);
        Frog frog = level.getPond().getFrog();
        LilyPad padTwo = padTwo(level);
        Hop hop = new Hop();
        Swim swim = new Swim();

        // Throw from the side of the reed's shadow, and walk to the edge, eating the
        // breadcrumb there.
        play(level, throwAndMove(ThrowableKind.TWIG, new Vec2(0.419, 0.126), new Walk(), L5_EDGE), L5_EDGE);
        // One long hop to pad 1, eating the minnow. The ripple reaches pad 2 and pushes it.
        play(level, move(hop, L5_PAD_1), L5_PAD_1);
        Vec2 pushed = padTwo.getPosition();
        assertTrue(pushed.getY() > L5_PAD_2.getY(), "pad 2 should have been pushed towards pad 1: " + pushed);
        assertTrue(pushed.distanceTo(L5_PAD_1) <= Rank.RANK_3.hopRange(), "pad 2 out of reach: " + pushed);
        // Hop to pad 2 where it now is, then swim the rest, eating the fly on the way.
        play(level, move(hop, pushed), pushed);
        play(level, move(swim, new Vec2(0.52, 0.26)), new Vec2(0.52, 0.26));
        play(level, move(swim, new Vec2(0.52, 0.165)), new Vec2(0.52, 0.165));
        play(level, move(swim, new Vec2(0.52, 0.075)), new Vec2(0.52, 0.075));

        assertInstanceOf(Won.class, level.getState());
        assertEquals(6, level.getTurn());
        assertEquals(0, level.getPond().elementsInOrder().stream().filter(Food.class::isInstance).count(),
                "all three foods eaten on the way");
        assertTrue(frog.hasEnergy());
        assertEquals(1, level.getBag().count(ThrowableKind.TWIG));
    }
}
