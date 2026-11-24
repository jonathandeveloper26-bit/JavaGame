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
        object.push(new Vec2(0, -4), 0.25, pond);

        assertAt(0.5, 0.25, object);
    }

    @Test
    void pushWorksDiagonally() {
        // (3, -4) has length 5, so a push of 0.25 moves 0.15 across and 0.2 up.
        object.push(new Vec2(3, -4), 0.25, pond);

        assertAt(0.65, 0.3, object);
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

        object.push(new Vec2(0, -1), 0.25, pond);

        assertAt(0.5625, 0.25, offCentre);
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
        assertThrows(NullPointerException.class, () -> object.push(new Vec2(1, 0), 0.25, null));
        assertAt(0.5, 0.5, object);
    }

    // Blocked pushes (GDD 3.1). The object is at (0.5, 0.5) with radius 0.125; every push
    // below is 0.125 to the right, to (0.625, 0.5), unless something is in the way.

    private static final Vec2 RIGHT = new Vec2(1, 0);

    /**
     * Where a blocker overlaps the object after the push: the object's right edge would
     * reach 0.75, and a blocker of radius 0.0625 here starts at 0.6875.
     */
    private static final Vec2 IN_THE_WAY = new Vec2(0.75, 0.5);

    @Test
    void aReedClumpInTheWayBlocksThePush() {
        pond.add(new Reed(IN_THE_WAY, 0.125));

        assertFalse(object.push(RIGHT, 0.125, pond));
        assertAt(0.5, 0.5, object);
    }

    @Test
    void anotherFloatingObjectInTheWayBlocksThePush() {
        pond.add(object);
        pond.add(new TestFloat(IN_THE_WAY.getX(), IN_THE_WAY.getY(), 0.125));

        assertFalse(object.push(RIGHT, 0.125, pond));
        assertAt(0.5, 0.5, object);
    }

    @Test
    void aTurtleInTheWayBlocksThePush() {
        pond.add(new Turtle(IN_THE_WAY, 0.125));

        assertFalse(object.push(RIGHT, 0.125, pond));
    }

    @Test
    void theObjectMayComeToRestJustTouchingSomething() {
        // At (0.625, 0.5) its edge is at 0.75, exactly where the reed's edge starts.
        pond.add(new Reed(new Vec2(0.8125, 0.5), 0.125));

        assertTrue(object.push(RIGHT, 0.125, pond));
        assertAt(0.625, 0.5, object);
    }

    @Test
    void theObjectDoesNotBlockItself() {
        pond.add(object);

        assertTrue(object.push(RIGHT, 0.125, pond));
    }

    @Test
    void somethingThatIsNotSolidDoesNotBlock() {
        pond.add(new TestElementThatIsNotSolid(new Vec2(0.6875, 0.5)));

        assertTrue(object.push(RIGHT, 0.125, pond));
    }

    @Test
    void aBankInTheWayBlocksThePush() {
        // Start bank from y about 0.542: the object's bottom edge would go from 0.625 past it.
        Pond withBanks = new Pond(Balance.PLAY_AREA_WIDTH, Balance.PLAY_AREA_HEIGHT, 0.125, 0.125, frog);
        FloatingObject nearTheBank = new TestFloat(0.5, 0.375, 0.25);

        assertFalse(nearTheBank.push(new Vec2(0, 1), 0.0625, withBanks));
        assertAt(0.5, 0.375, nearTheBank);
    }

    @Test
    void theSideOfThePlayAreaBlocksThePush() {
        FloatingObject nearTheSide = new TestFloat(0.875, 0.5, 0.125);

        assertFalse(nearTheSide.push(RIGHT, 0.125, pond));
    }

    @Test
    void aBlockedPushLeavesTheRiderWhereItIs() {
        frog.standOn(object);
        pond.add(new Reed(IN_THE_WAY, 0.125));

        object.push(RIGHT, 0.125, pond);

        assertAt(0.5, 0.5, frog);
        assertSame(object, frog.standingOn().orElseThrow());
    }

    @Test
    void solidElements() {
        assertTrue(object.isSolid());
        assertTrue(new Reed(new Vec2(0, 0), 0.1).isSolid());
        assertTrue(new Turtle(new Vec2(0, 0), 0.1).isSolid());
        assertFalse(frog.isSolid());
        assertFalse(new TestElementThatIsNotSolid(new Vec2(0, 0)).isSolid());
    }

    /** An element that keeps the default: not solid, like food or an icon. */
    private static final class TestElementThatIsNotSolid extends AbstractElement {
        TestElementThatIsNotSolid(Vec2 position) {
            super(position, 0.125);
        }
    }
}
