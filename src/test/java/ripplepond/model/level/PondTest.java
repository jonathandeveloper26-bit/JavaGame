package ripplepond.model.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import ripplepond.model.element.Element;
import ripplepond.model.element.Frog;
import ripplepond.model.element.LilyPad;
import ripplepond.model.element.Reed;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.progress.Rank;

class PondTest {

    private static final double HEIGHT = 2.0 / 3.0;
    private static final double BANK = 0.1;

    /**
     * A frog for the pond's constructor, in the top-left corner. Pond does not check
     * the frog's position yet (isFree's frog check waits on J2 being wired in).
     */
    private static Frog frog() {
        return new Frog(new Vec2(0, 0), Rank.RANK_1);
    }

    private static Pond pond() {
        return new Pond(1, HEIGHT, BANK, BANK, frog());
    }

    private static Reed reedAt(double x, double y) {
        return new Reed(new Vec2(x, y), 0.1);
    }

    @Test
    void areaAtReturnsBanksAndWater() {
        Pond pond = pond();
        assertEquals(AreaType.FAR_BANK, pond.areaAt(new Vec2(0.5, 0.05)));
        assertEquals(AreaType.WATER, pond.areaAt(new Vec2(0.5, 0.3)));
        assertEquals(AreaType.START_BANK, pond.areaAt(new Vec2(0.5, 0.6)));
    }

    @Test
    void areaAtRejectsPointsOutsideThePlayArea() {
        assertThrows(IllegalArgumentException.class, () -> pond().areaAt(new Vec2(1.5, 0.3)));
    }

    @Test
    void constructorRejectsOverlappingBanks() {
        assertThrows(IllegalArgumentException.class,
                () -> new Pond(1, HEIGHT, 0.4, 0.4, frog()));
    }

    @Test
    void isFreeAcceptsOpenWater() {
        assertTrue(pond().isFree(new Vec2(0.5, 0.45), 0.1));
    }

    @Test
    void isFreeRejectsCircleThatReachesTheEdgeOfThePlayArea() {
        assertFalse(pond().isFree(new Vec2(0.02, 0.3), 0.1));
    }

    @Test
    void isFreeRejectsCircleOnABank() {
        assertFalse(pond().isFree(new Vec2(0.5, 0.05), 0.1));
    }

    @Test
    void isFreeRejectsOverlapWithAnElement() {
        Pond pond = pond();
        pond.add(reedAt(0.5, 0.3));
        assertFalse(pond.isFree(new Vec2(0.5, 0.35), 0.1));
    }

    @Test
    void isFreeRejectsAnExactTouch() {
        // Binary-exact numbers, so the distance is exactly the sum of the radii (0.125).
        // Decimal values like 0.3 and 0.4 are not exact in floating point, which made this test fail.
        Pond pond = pond();
        pond.add(new Reed(new Vec2(0.5, 0.25), 0.125));
        assertFalse(pond.isFree(new Vec2(0.5, 0.375), 0.125));
    }

    @Test
    void reedBetweenIsTrueWhenAReedBlocksTheLine() {
        Pond pond = pond();
        pond.add(reedAt(0.5, 0.3));
        assertTrue(pond.reedBetween(new Vec2(0.2, 0.3), new Vec2(0.8, 0.3)));
    }

    @Test
    void reedBetweenIsFalseWhenTheReedIsOffTheLine() {
        Pond pond = pond();
        pond.add(reedAt(0.5, 0.3));
        assertFalse(pond.reedBetween(new Vec2(0.2, 0.5), new Vec2(0.8, 0.5)));
    }

    @Test
    void elementsInOrderSortsTopToBottomThenLeftToRight() {
        Pond pond = pond();
        Reed bottom = reedAt(0.5, 0.5);
        Reed topRight = reedAt(0.8, 0.2);
        Reed topLeft = reedAt(0.2, 0.2);
        pond.add(bottom);
        pond.add(topRight);
        pond.add(topLeft);

        List<Element> ordered = pond.elementsInOrder();

        assertEquals(Arrays.asList(topLeft, topRight, bottom), ordered);
    }

    @Test
    void elementsInOrderReturnsANewListEachTime() {
        Pond pond = pond();
        pond.add(reedAt(0.5, 0.5));
        pond.add(reedAt(0.1, 0.1));

        List<Element> first = pond.elementsInOrder();
        List<Element> second = pond.elementsInOrder();

        assertEquals(first, second);
        assertTrue(first != second);
    }

    @Test
    void removeTakesTheElementOutOfTheOrder() {
        Pond pond = pond();
        Reed reed = reedAt(0.5, 0.3);
        pond.add(reed);
        pond.remove(reed);
        assertTrue(pond.elementsInOrder().isEmpty());
    }

    @Test
    void edgeBetweenFarBankAndWaterBelongsToWater() {
        assertEquals(AreaType.WATER, pond().areaAt(new Vec2(0.5, BANK)));
    }

    @Test
    void edgeBetweenWaterAndStartBankBelongsToStartBank() {
        assertEquals(AreaType.START_BANK, pond().areaAt(new Vec2(0.5, HEIGHT - BANK)));
    }

    @Test
    void bottomEdgeOfThePlayAreaIsStartBank() {
        assertEquals(AreaType.START_BANK, pond().areaAt(new Vec2(0.5, HEIGHT)));
    }

    @Test
    void pondWithoutBanksIsAllWater() {
        Pond pond = new Pond(1, HEIGHT, frog());
        assertEquals(AreaType.WATER, pond.areaAt(new Vec2(0.5, 0)));
        assertEquals(AreaType.WATER, pond.areaAt(new Vec2(0.5, HEIGHT)));
    }

    @Test
    void zeroDepthStartBankLeavesTheBottomEdgeInTheWater() {
        Pond pond = new Pond(1, HEIGHT, BANK, 0, frog());
        assertEquals(AreaType.WATER, pond.areaAt(new Vec2(0.5, HEIGHT)));
    }

    @Test
    void surfaceAtFindsTheObjectUnderAPoint() {
        Pond pond = pond();
        LilyPad pad = new LilyPad(new Vec2(0.5, 0.25), 0.125);
        pond.add(pad);

        assertSame(pad, pond.surfaceAt(new Vec2(0.53125, 0.25)).orElseThrow());
        assertTrue(pond.surfaceAt(new Vec2(0.75, 0.25)).isEmpty());
    }

    @Test
    void surfaceAtIgnoresElementsThatDoNotFloat() {
        Pond pond = pond();
        pond.add(reedAt(0.5, 0.25));

        assertTrue(pond.surfaceAt(new Vec2(0.5, 0.25)).isEmpty());
    }

    @Test
    void surfaceAtPicksTheNearestOfTwoOverlappingObjects() {
        Pond pond = pond();
        LilyPad left = new LilyPad(new Vec2(0.5, 0.25), 0.25);
        LilyPad right = new LilyPad(new Vec2(0.625, 0.25), 0.25);
        pond.add(left);
        pond.add(right);

        assertSame(right, pond.surfaceAt(new Vec2(0.59375, 0.25)).orElseThrow());
    }
}
