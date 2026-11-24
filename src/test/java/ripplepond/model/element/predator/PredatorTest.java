package ripplepond.model.element.predator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ripplepond.model.Balance;
import ripplepond.model.element.Frog;
import ripplepond.model.element.Reed;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.AreaType;
import ripplepond.model.level.Pond;
import ripplepond.model.progress.Rank;

class PredatorTest {

    // Far bank 0 to 0.10, water 0.10 to 0.567, start bank 0.567 to 0.667.
    private static Pond pond(Vec2 frogAt) {
        return new Pond(1, 2.0 / 3.0, 0.10, 0.10, new Frog(frogAt, Rank.forXp(0)));
    }

    @Test
    void snakeMovesOnlyOnEvenTurns() {
        // Both on the start bank, so the snake has a legal step towards the frog.
        Pond pond = pond(new Vec2(0.5, 0.62));
        Snake snake = new Snake(new Vec2(0.2, 0.62), Balance.SNAKE_SIZE);
        pond.add(snake);

        snake.act(pond, 1);
        assertEquals(0.2, snake.getPosition().getX(), 1e-9, "no move on turn 1");

        snake.act(pond, 2);
        assertTrue(snake.getPosition().getX() > 0.2, "moves on turn 2");
    }

    @Test
    void snakeNeverEntersWater() {
        // The frog is in the water; the snake starts on the start bank and tries to reach it.
        Pond pond = pond(new Vec2(0.5, 0.30));
        Snake snake = new Snake(new Vec2(0.2, 0.62), Balance.SNAKE_SIZE);
        pond.add(snake);
        for (int turn = 2; turn <= 60; turn += 2) {
            snake.act(pond, turn);
            assertFalse(pond.areaAt(snake.getPosition()) == AreaType.WATER,
                    "snake centre entered water on turn " + turn);
        }
    }

    @Test
    void snakeMayEnterReedInWater() {
        Pond pond = pond(new Vec2(0.5, 0.62));
        pond.add(new Reed(new Vec2(0.5, 0.30), 0.10));
        Snake snake = new Snake(new Vec2(0.2, 0.62), Balance.SNAKE_SIZE);
        assertTrue(snake.isLegalAt(pond, new Vec2(0.5, 0.30)), "inside a reed");
        assertFalse(snake.isLegalAt(pond, new Vec2(0.5, 0.20)), "open water");
    }

    @Test
    void birdFliesOverWater() {
        Pond pond = pond(new Vec2(0.5, 0.30));
        Bird bird = new Bird(new Vec2(0.2, 0.30), Balance.BIRD_SIZE);
        pond.add(bird);
        bird.act(pond, 2);
        assertTrue(bird.getPosition().getX() > 0.2, "bird moves towards the frog over water");
    }

    @Test
    void birdMayBeOverAnyArea() {
        Pond pond = pond(new Vec2(0.5, 0.30));
        Bird bird = new Bird(new Vec2(0.2, 0.30), Balance.BIRD_SIZE);
        assertTrue(bird.isLegalAt(pond, new Vec2(0.3, 0.05)), "over the far bank");
        assertTrue(bird.isLegalAt(pond, new Vec2(0.3, 0.30)), "over water");
        assertTrue(bird.isLegalAt(pond, new Vec2(0.3, 0.62)), "over the start bank");
    }

    @Test
    void fishStaysOutOfReeds() {
        Pond pond = pond(new Vec2(0.5, 0.62));
        Fish fish = new Fish(new Vec2(0.2, 0.30), Balance.FISH_SIZE);
        pond.add(new Reed(new Vec2(0.5, 0.30), 0.10));
        assertFalse(fish.isLegalAt(pond, new Vec2(0.5, 0.30)), "inside a reed");
        assertTrue(fish.isLegalAt(pond, new Vec2(0.2, 0.30)), "open water");
    }

    @Test
    void fishStaysOutOfBanks() {
        Pond pond = pond(new Vec2(0.5, 0.30));
        Fish fish = new Fish(new Vec2(0.2, 0.30), Balance.FISH_SIZE);
        assertFalse(fish.isLegalAt(pond, new Vec2(0.3, 0.05)), "far bank");
        assertFalse(fish.isLegalAt(pond, new Vec2(0.3, 0.62)), "start bank");
    }

    @Test
    void attacksOnEveryTurnWhenTouchingTheFrog() {
        Pond pond = pond(new Vec2(0.5, 0.56));
        // Bird radius 0.03 + frog radius 0.02 = 0.05. Centres 0.04 apart: clearly touching.
        Bird bird = new Bird(new Vec2(0.5, 0.52), Balance.BIRD_SIZE);
        pond.add(bird);
        Frog frog = pond.getFrog();
        int start = frog.getHealth();
        assertEquals(Balance.BIRD_DAMAGE, bird.act(pond, 1), "attacks on an odd turn");
        assertEquals(Balance.BIRD_DAMAGE, bird.act(pond, 3), "and on any turn");
        assertEquals(start - 2 * Balance.BIRD_DAMAGE, frog.getHealth());
    }

    @Test
    void noAttackWhenOutOfReach() {
        Pond pond = pond(new Vec2(0.5, 0.30));
        Bird bird = new Bird(new Vec2(0.2, 0.30), Balance.BIRD_SIZE);
        pond.add(bird);
        int start = pond.getFrog().getHealth();
        assertEquals(0, bird.act(pond, 2), "moves on turn 2 but is still too far to bite");
        assertEquals(start, pond.getFrog().getHealth());
    }

    @Test
    void healthNeverDropsBelowZero() {
        Pond pond = pond(new Vec2(0.5, 0.56));
        Frog frog = pond.getFrog();
        frog.takeDamage(frog.getHealth() - 1); // leave 1 HP
        Bird bird = new Bird(new Vec2(0.5, 0.52), Balance.BIRD_SIZE);
        pond.add(bird);
        bird.act(pond, 1);
        assertEquals(0, frog.getHealth());
    }
}