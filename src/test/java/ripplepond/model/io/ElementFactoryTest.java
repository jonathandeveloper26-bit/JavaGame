package ripplepond.model.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ripplepond.model.element.Element;
import ripplepond.model.element.LilyPad;
import ripplepond.model.element.Reed;
import ripplepond.model.element.ThrowableKind;
import ripplepond.model.element.ThrownObject;
import ripplepond.model.element.Turtle;
import ripplepond.model.element.pickup.Breadcrumb;
import ripplepond.model.element.pickup.Fly;
import ripplepond.model.element.pickup.Minnow;
import ripplepond.model.element.predator.Bird;
import ripplepond.model.element.predator.Fish;
import ripplepond.model.element.predator.Snake;
import ripplepond.model.geometry.Vec2;

/**
 * Tests for {@link ElementFactory}: each type name makes the right class at the given
 * position and size, defaults match the level format, and unknown names are refused.
 */
class ElementFactoryTest {

    private static final double DELTA = 1e-12;
    private static final Vec2 AT = new Vec2(0.25, 0.5);

    private final ElementFactory factory = new ElementFactory();

    @Test
    void thrownObjectNamesMakeThrownObjectsOfThatKind() throws LevelFormatException {
        assertEquals(ThrowableKind.TWIG, thrown("twig").getKind());
        assertEquals(ThrowableKind.PLANK, thrown("plank").getKind());
        assertEquals(ThrowableKind.LOG, thrown("log").getKind());
    }

    private ThrownObject thrown(String name) throws LevelFormatException {
        return assertInstanceOf(ThrownObject.class, factory.create(name, AT, 0.125));
    }

    @Test
    void otherNamesMakeTheirOwnClass() throws LevelFormatException {
        assertInstanceOf(LilyPad.class, factory.create("lilypad", AT, 0.125));
        assertInstanceOf(Turtle.class, factory.create("turtle", AT, 0.125));
        assertInstanceOf(Reed.class, factory.create("reed", AT, 0.125));
        assertInstanceOf(Fly.class, factory.create("fly", AT, 0.125));
        assertInstanceOf(Minnow.class, factory.create("minnow", AT, 0.125));
        assertInstanceOf(Breadcrumb.class, factory.create("breadcrumb", AT, 0.125));
    }

    @Test
    void predatorNamesMakeTheirOwnClass() throws LevelFormatException {
        assertInstanceOf(Snake.class, factory.create("snake", AT, 0.08));
        assertInstanceOf(Bird.class, factory.create("bird", AT, 0.06));
        assertInstanceOf(Fish.class, factory.create("fish", AT, 0.06));
    }

    @Test
    void theElementHasTheGivenPositionAndSize() throws LevelFormatException {
        Element pad = factory.create("lilypad", AT, 0.125);

        assertEquals(AT, pad.getPosition());
        assertEquals(0.125, pad.getSize(), DELTA);
    }

    @Test
    void defaultSizesMatchTheLevelFormat() throws LevelFormatException {
        assertEquals(0.05, factory.defaultSize("twig"), DELTA);
        assertEquals(0.10, factory.defaultSize("plank"), DELTA);
        assertEquals(0.18, factory.defaultSize("log"), DELTA);
        assertEquals(0.08, factory.defaultSize("lilypad"), DELTA);
        assertEquals(0.07, factory.defaultSize("turtle"), DELTA);
        assertEquals(0.10, factory.defaultSize("reed"), DELTA);
        assertEquals(0.025, factory.defaultSize("fly"), DELTA);
        assertEquals(0.03, factory.defaultSize("minnow"), DELTA);
        assertEquals(0.025, factory.defaultSize("breadcrumb"), DELTA);
    }

    @Test
    void anUnknownNameIsRefusedWithTheNameInTheMessage() {
        LevelFormatException error = assertThrows(LevelFormatException.class,
                () -> factory.create("frogg", AT, 0.125));

        assertEquals("unknown element type 'frogg'", error.getMessage());
        assertThrows(LevelFormatException.class, () -> factory.defaultSize("frogg"));
    }

    @Test
    void namesAreCaseSensitive() {
        // The level format says keywords and type names are lower case.
        assertFalse(factory.knows("Log"));
        assertThrows(LevelFormatException.class, () -> factory.create("Log", AT, 0.125));
    }

    @Test
    void iconsAreNotElementTypes() {
        // Throwable icons use their own line format and are handled by the loader.
        assertFalse(factory.knows("icon"));
    }

    @Test
    void aNewTypeCanBeRegistered() throws LevelFormatException {
        factory.register("bigpad", 0.25, LilyPad::new);

        assertTrue(factory.knows("bigpad"));
        assertEquals(0.25, factory.defaultSize("bigpad"), DELTA);
        assertInstanceOf(LilyPad.class, factory.create("bigpad", AT, 0.25));
    }

    @Test
    void aNameCannotBeRegisteredTwice() {
        assertThrows(IllegalArgumentException.class, () -> factory.register("log", 0.25, LilyPad::new));
    }

    @Test
    void registerRejectsBadArguments() {
        assertThrows(NullPointerException.class, () -> factory.register(null, 0.25, LilyPad::new));
        assertThrows(NullPointerException.class, () -> factory.register("x", 0.25, null));
        assertThrows(IllegalArgumentException.class, () -> factory.register("x", 0, LilyPad::new));
    }

    @Test
    void createRejectsBadArguments() {
        assertThrows(NullPointerException.class, () -> factory.create("log", null, 0.125));
        assertThrows(IllegalArgumentException.class, () -> factory.create("log", AT, 0));
    }

    @Test
    void eachCallMakesANewElement() throws LevelFormatException {
        Element first = factory.create("reed", AT, 0.125);
        Element second = factory.create("reed", AT, 0.125);

        assertNotSame(first, second);
        assertInstanceOf(Reed.class, second);
    }
}