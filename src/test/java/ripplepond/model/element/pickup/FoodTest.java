package ripplepond.model.element.pickup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import ripplepond.model.element.Frog;
import ripplepond.model.event.EventType;
import ripplepond.model.event.GameEvent;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.InProgress;
import ripplepond.model.level.Level;
import ripplepond.model.level.Pond;
import ripplepond.model.level.ThrowBag;
import ripplepond.model.progress.Rank;

/**
 * Tests for {@link Food} and its three kinds (GDD 2.5): each restores the right stats by
 * the right amount, never above the maximum, and is gone from the level once eaten.
 * <p>
 * The frog is rank 1 (at most 10 HP and 20 energy). Most tests first bring it down to
 * 5 HP and 10 energy, so there is room to restore.
 */
class FoodTest {

    private static final double DELTA = 1e-12;
    private static final Vec2 AT = new Vec2(0.5, 0.5);

    private final Frog frog = new Frog(AT, Rank.RANK_1);
    private final Pond pond = new Pond(Pond.WIDTH, Pond.HEIGHT, frog);
    private final Level level = new Level(pond, new ThrowBag(), 40, new InProgress());
    private final List<GameEvent> events = new ArrayList<>();

    FoodTest() {
        level.addListener(events::add);
    }

    private void tireTheFrog() {
        frog.takeDamage(5);
        frog.spendEnergy(10);
    }

    private void eat(Food food) {
        pond.add(food);
        food.collect(frog, level);
    }

    // What each food restores

    @Test
    void aFlyRestoresTwoHealthAndFourEnergy() {
        tireTheFrog();

        eat(new Fly(AT));

        assertEquals(7, frog.getHealth());
        assertEquals(14, frog.getEnergy(), DELTA);
    }

    @Test
    void aMinnowRestoresFiveEnergyAndNoHealth() {
        tireTheFrog();

        eat(new Minnow(AT));

        assertEquals(5, frog.getHealth());
        assertEquals(15, frog.getEnergy(), DELTA);
    }

    @Test
    void aBreadcrumbRestoresThreeHealthAndNoEnergy() {
        tireTheFrog();

        eat(new Breadcrumb(AT));

        assertEquals(8, frog.getHealth());
        assertEquals(10, frog.getEnergy(), DELTA);
    }

    @Test
    void foodNeverRaisesAStatAboveTheMaximum() {
        frog.takeDamage(1);
        frog.spendEnergy(1);

        eat(new Fly(AT));
        eat(new Minnow(AT));
        eat(new Breadcrumb(AT));

        assertEquals(10, frog.getHealth());
        assertEquals(20, frog.getEnergy(), DELTA);
    }

    // What eating does to the level

    @Test
    void eatenFoodIsGoneFromTheLevel() {
        Fly fly = new Fly(AT);

        eat(fly);

        assertFalse(pond.elementsInOrder().contains(fly));
    }

    @Test
    void eatingPublishesFoodEaten() {
        Minnow minnow = new Minnow(AT);

        eat(minnow);

        assertEquals(1, events.size());
        assertEquals(EventType.FOOD_EATEN, events.get(0).getType());
        assertSame(minnow, events.get(0).getSubject());
        assertEquals(1, events.get(0).getAmount(), DELTA);
    }

    @Test
    void everyFoodIsEatenTheSameWay() {
        // Template method: one collect for every kind; only applyTo differs.
        List<Food> foods = List.of(new Fly(AT), new Minnow(AT), new Breadcrumb(AT));

        for (Food food : foods) {
            eat(food);
        }

        assertEquals(3, events.size());
        assertEquals(0, pond.elementsInOrder().size());
    }

    // The foods themselves

    @Test
    void foodsHaveTheirUsualSizes() {
        assertEquals(0.025, new Fly(AT).getSize(), DELTA);
        assertEquals(0.03, new Minnow(AT).getSize(), DELTA);
        assertEquals(0.025, new Breadcrumb(AT).getSize(), DELTA);
        assertEquals(0.05, new Fly(AT, 0.05).getSize(), DELTA);
    }

    @Test
    void foodIsNotSolid() {
        assertFalse(new Fly(AT).isSolid());
    }

    @Test
    void rejectsBadArguments() {
        Fly fly = new Fly(AT);

        assertThrows(NullPointerException.class, () -> fly.collect(null, level));
        assertThrows(NullPointerException.class, () -> fly.collect(frog, null));
        assertThrows(NullPointerException.class, () -> new Breadcrumb(null));
        assertThrows(IllegalArgumentException.class, () -> new Minnow(AT, 0));
    }
}
