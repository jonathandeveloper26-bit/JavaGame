package ripplepond.model.element;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ripplepond.model.Balance;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.Pond;
import ripplepond.model.progress.Rank;

/**
 * Tests for {@link FloatingObject}: covering a point, the link with the frog standing on
 * it, and pushing with the riding rule. The object is centred at (0.5, 0.5) with radius
 * 0.125; the numbers are exact in binary so edge cases land exactly on the edge.
 */
class FloatingObjectTest {

    private static final double DELTA = 1e-12;

    /** The smallest possible concrete floating object. */
    private static final class TestFloat extends FloatingObject {
        TestFloat(double x, double y, double size) {
            super(new Vec2(x, y), size);
        }
    }

    private final FloatingObject object = new TestFloat(0.5, 0.5, 0.25);
    private final Frog frog = new Frog(new Vec2(0.5, 0.5), Rank.RANK_1);
    private final Pond pond = new Pond(Balance.PLAY_AREA_WIDTH, Balance.PLAY_AREA_HEIGHT, frog);

    private static void assertAt(double x, double y, Element element) {
        assertEquals(x, element.getPosition().getX(), DELTA, "x");
        assertEquals(y, element.getPosition().getY(), DELTA, "y");
    }

    // covers

    @Test
    void coversItsCentreAndItsEdge() {
        assertTrue(object.covers(new Vec2(0.5, 0.5)));
        assertTrue(object.covers(new Vec2(0.625, 0.5)));
    }

    @Test
    void doesNotCoverAPointJustOutside() {
        assertFalse(object.covers(new Vec2(0.625 + 1e-9, 0.5)));
    }

    @Test
    void coversRejectsNull() {
        assertThrows(NullPointerException.class, () -> object.covers(null));
    }

    // The link between the frog and the object

    @Test
    void aNewObjectHasNoRider() {
        assertTrue(object.getRider().isEmpty());
    }

    @Test
    void standingOnAnObjectLinksBothSides() {
        frog.standOn(object);

        assertSame(object, frog.standingOn().orElseThrow());
        assertSame(frog, object.getRider().orElseThrow());
    }

    @Test
    void steppingOffClearsBothSides() {
        frog.standOn(object);
        frog.standOn(null);

        assertTrue(frog.standingOn().isEmpty());
        assertTrue(object.getRider().isEmpty());
    }

    @Test
    void movingToAnotherObjectReleasesTheFirst() {
        FloatingObject other = new TestFloat(0.5, 0.625, 0.5);
        frog.standOn(object);
        frog.standOn(other);

        assertSame(other, frog.standingOn().orElseThrow());
        assertSame(frog, other.getRider().orElseThrow());
        assertTrue(object.getRider().isEmpty());
    }

    @Test
    void cannotStandOnAnObjectThatIsNotUnderTheFrog() {
        FloatingObject farAway = new TestFloat(0.125, 0.125, 0.125);

        assertThrows(IllegalArgumentException.class, () -> frog.standOn(farAway));
        assertTrue(frog.standingOn().isEmpty());
        assertTrue(farAway.getRider().isEmpty());
    }

    // push

    @Test
    void pushMovesTheObjectAlongTheDirection() {
        assertTrue(object.push(new Vec2(1, 0), 0.25, pond));

        assertAt(0.75, 0.5, object);
    }

    @Test
    void onlyTheDirectionOfTheVectorCountsNotItsLength() {
        object.push(new Vec2(0, 4), 0.25, pond);

        assertAt(0.5, 0.75, object);
    }

    @Test
    void pushWorksDiagonally() {
        // (3, 4) has length 5, so a push of 0.5 moves 0.3 across and 0.4 down.
        object.push(new Vec2(3, 4), 0.5, pond);

        assertAt(0.8, 0.9, object);
    }

    @Test
    void theRiderMovesWithTheObject() {
        frog.standOn(object);

        object.push(new Vec2(-1, 0), 0.25, pond);

        assertAt(0.25, 0.5, frog);
        assertSame(object, frog.standingOn().orElseThrow());
        assertTrue(object.covers(frog.getPosition()));
    }

    @Test
    void aRiderAwayFromTheCentreKeepsItsSpotOnTheObject() {
        Frog offCentre = new Frog(new Vec2(0.5625, 0.5), Rank.RANK_1);
        offCentre.standOn(object);

        object.push(new Vec2(0, 1), 0.25, pond);

        assertAt(0.5625, 0.75, offCentre);
    }

    @Test
    void aFrogThatIsNotRidingStaysWhereItIs() {
        object.push(new Vec2(1, 0), 0.25, pond);

        assertAt(0.5, 0.5, frog);
    }

    @Test
    void aZeroPushDoesNotMoveAnything() {
        frog.standOn(object);

        assertFalse(object.push(new Vec2(1, 0), 0, pond));
        assertFalse(object.push(new Vec2(0, 0), 0.25, pond));
        assertAt(0.5, 0.5, object);
        assertAt(0.5, 0.5, frog);
    }

    @Test
    void pushRejectsBadArguments() {
        assertThrows(IllegalArgumentException.class, () -> object.push(new Vec2(1, 0), -0.1, pond));
        assertThrows(IllegalArgumentException.class,
                () -> object.push(new Vec2(1, 0), Double.NaN, pond));
        assertThrows(NullPointerException.class, () -> object.push(null, 0.25, pond));
        assertAt(0.5, 0.5, object);
    }
}
