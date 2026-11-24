package ripplepond.model.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ripplepond.model.element.Frog;
import ripplepond.model.element.ThrowableKind;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.InProgress;
import ripplepond.model.level.Level;
import ripplepond.model.level.TurnAction;
import ripplepond.model.level.Won;
import ripplepond.model.movement.Hop;
import ripplepond.model.movement.MoveMode;
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
}
