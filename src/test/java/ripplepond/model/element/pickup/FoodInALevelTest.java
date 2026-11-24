package ripplepond.model.element.pickup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import ripplepond.model.element.Frog;
import ripplepond.model.event.EventType;
import ripplepond.model.event.GameEvent;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.io.LevelFormatException;
import ripplepond.model.io.LevelLoader;
import ripplepond.model.level.Level;
import ripplepond.model.level.TurnAction;
import ripplepond.model.movement.Walk;
import ripplepond.model.progress.Rank;

/**
 * X1's finish line: the three foods work in a test level. {@code levels/food.txt} puts a
 * fly, a minnow and a breadcrumb in a row along the start bank, 0.10 apart; the frog
 * walks over them one per turn through the real turn engine.
 */
class FoodInALevelTest {

    private static final double DELTA = 1e-9;

    @Test
    void theLevelFileLoadsAllThreeFoods() throws LevelFormatException {
        Level level = new LevelLoader().load("levels/food.txt", Rank.RANK_1);

        List<?> elements = level.getPond().elementsInOrder();
        assertEquals(3, elements.size());
        assertInstanceOf(Fly.class, elements.get(0));
        assertInstanceOf(Minnow.class, elements.get(1));
        assertInstanceOf(Breadcrumb.class, elements.get(2));
    }

    @Test
    void walkingOverTheFoodEatsEachOneInTurn() throws LevelFormatException {
        Level level = new LevelLoader().load("levels/food.txt", Rank.RANK_1);
        Frog frog = level.getPond().getFrog();
        List<GameEvent> events = new ArrayList<>();
        level.addListener(events::add);
        frog.takeDamage(6);

        // Each walk of 0.10 costs 1 energy.
        level.playTurn(walkTo(0.30));
        assertEquals(4 + 2, frog.getHealth(), "the fly gives 2 HP");
        assertEquals(20, frog.getEnergy(), DELTA, "the fly's 4 energy is capped at 20");

        level.playTurn(walkTo(0.40));
        assertEquals(6, frog.getHealth(), "the minnow gives no HP");
        assertEquals(20, frog.getEnergy(), DELTA, "the minnow's 5 energy is capped at 20");

        level.playTurn(walkTo(0.50));
        assertEquals(6 + 3, frog.getHealth(), "the breadcrumb gives 3 HP");
        assertEquals(19, frog.getEnergy(), DELTA, "the breadcrumb gives no energy");

        assertTrue(level.getPond().elementsInOrder().isEmpty(), "every food was eaten");
        assertEquals(3, events.stream().filter(e -> e.getType() == EventType.FOOD_EATEN).count());
    }

    private static TurnAction walkTo(double x) {
        return new TurnAction(null, null, new Walk(), new Vec2(x, 0.60));
    }
}
