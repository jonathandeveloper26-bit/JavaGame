package ripplepond.model.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.List;

import org.junit.jupiter.api.Test;

import ripplepond.model.element.Element;
import ripplepond.model.element.Frog;
import ripplepond.model.element.LilyPad;
import ripplepond.model.element.Reed;
import ripplepond.model.element.ThrowableKind;
import ripplepond.model.element.Turtle;
import ripplepond.model.element.pickup.ThrowableIcon;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.AreaType;
import ripplepond.model.level.InProgress;
import ripplepond.model.level.Level;
import ripplepond.model.level.Pond;
import ripplepond.model.level.TurnAction;
import ripplepond.model.progress.Rank;

/**
 * Tests for {@link LevelLoader}. The files in {@code src/test/resources/levels/} cover a
 * valid level and the main errors; shorter cases are written inline.
 */
class LevelLoaderTest {

    private static final double DELTA = 1e-12;

    /** The settings every inline level needs; tests add the lines they are about. */
    private static final String SETTINGS = """
            turns 40
            bank far 0.10
            bank start 0.10
            frog 0.50 0.62
            """;

    private final LevelLoader loader = new LevelLoader();

    private Level load(String text) throws LevelFormatException {
        return loader.load(new StringReader(text), Rank.RANK_1);
    }

    /** Loads text that must be refused, checks the line number, and returns the message. */
    private String errorFor(String text, int expectedLine) {
        LevelFormatException error = assertThrows(LevelFormatException.class, () -> load(text));
        assertEquals(expectedLine, error.getLineNumber(), error.getMessage());
        return error.getMessage();
    }

    private String fileErrorFor(String path, int expectedLine) {
        LevelFormatException error = assertThrows(LevelFormatException.class,
                () -> loader.load(path, Rank.RANK_1));
        assertEquals(expectedLine, error.getLineNumber(), error.getMessage());
        return error.getMessage();
    }

    private static void assertContains(String expected, String message) {
        assertTrue(message.contains(expected), () -> "'" + message + "' should contain '" + expected + "'");
    }

    // A valid level

    @Test
    void theExampleLevelLoadsIntoALevelAtTurnZero() throws LevelFormatException {
        Level level = loader.load("levels/valid.txt", Rank.RANK_1);

        assertEquals(0, level.getTurn());
        assertEquals(40, level.getTurnLimit());
        assertInstanceOf(InProgress.class, level.getState());
    }

    @Test
    void theBanksAreWhereTheFileSays() throws LevelFormatException {
        Pond pond = loader.load("levels/valid.txt", Rank.RANK_1).getPond();

        assertEquals(AreaType.FAR_BANK, pond.areaAt(new Vec2(0.5, 0.05)));
        assertEquals(AreaType.WATER, pond.areaAt(new Vec2(0.5, 0.3)));
        assertEquals(AreaType.START_BANK, pond.areaAt(new Vec2(0.5, 0.62)));
    }

    @Test
    void theFrogStartsWhereTheFileSaysWithFullHealthAndEnergy() throws LevelFormatException {
        Frog frog = loader.load("levels/valid.txt", Rank.RANK_1).getPond().getFrog();

        assertEquals(new Vec2(0.50, 0.62), frog.getPosition());
        assertEquals(Rank.RANK_1.maxHealth(), frog.getHealth());
        assertEquals(Rank.RANK_1.maxEnergy(), frog.getEnergy(), DELTA);
    }

    @Test
    void theRankSetsTheFrogsMaximums() throws LevelFormatException {
        Frog frog = loader.load(new StringReader(SETTINGS), Rank.RANK_3).getPond().getFrog();

        assertEquals(Rank.RANK_3, frog.getRank());
        assertEquals(14, frog.getHealth());
    }

    @Test
    void everyElementIsAddedWithItsTypePositionAndSize() throws LevelFormatException {
        List<Element> elements = loader.load("levels/valid.txt", Rank.RANK_1).getPond().elementsInOrder();

        assertEquals(5, elements.size());
        // In turn order: top to bottom, then left to right; the pad and the icon at the
        // same spot stay in file order.
        assertInstanceOf(Reed.class, elements.get(0));
        assertInstanceOf(Turtle.class, elements.get(1));
        assertEquals(0.09, elements.get(1).getSize(), DELTA);
        assertInstanceOf(LilyPad.class, elements.get(2));
        assertEquals(new Vec2(0.42, 0.34), elements.get(2).getPosition());
        assertInstanceOf(ThrowableIcon.class, elements.get(3));
        assertInstanceOf(LilyPad.class, elements.get(4));
    }

    @Test
    void theBagHoldsEveryBagLine() throws LevelFormatException {
        Level level = loader.load("levels/valid.txt", Rank.RANK_1);

        assertEquals(2, level.getBag().count(ThrowableKind.TWIG));
        assertEquals(1, level.getBag().count(ThrowableKind.PLANK));
        assertEquals(0, level.getBag().count(ThrowableKind.LOG));
    }

    @Test
    void bagLinesForTheSameKindAddUp() throws LevelFormatException {
        Level level = load(SETTINGS + "bag log 1\nbag log 2\n");

        assertEquals(3, level.getBag().count(ThrowableKind.LOG));
    }

    @Test
    void aLevelWithNoBagLineHasAnEmptyBag() throws LevelFormatException {
        assertTrue(load(SETTINGS).getBag().isEmpty());
    }

    @Test
    void anElementWithoutADiameterGetsTheDefaultSize() throws LevelFormatException {
        Element log = load(SETTINGS + "log 0.5 0.3\n").getPond().elementsInOrder().get(0);

        assertEquals(ThrowableKind.LOG.size(), log.getSize(), DELTA);
    }

    @Test
    void commentsBlankLinesExtraSpacesAndAnyOrderAreAccepted() throws LevelFormatException {
        Level level = load("""
                # a comment line

                   reed   0.20   0.30      # an element before the settings
                frog 0.50 0.62
                bank start 0.10
                turns 12
                bank far 0.10
                """);

        assertEquals(12, level.getTurnLimit());
        assertEquals(1, level.getPond().elementsInOrder().size());
    }

    @Test
    void theLoadedLevelCanBePlayed() throws LevelFormatException {
        Level level = loader.load("levels/valid.txt", Rank.RANK_1);

        level.playTurn(new TurnAction(null, null, null, null));

        assertEquals(1, level.getTurn());
        assertFalse(level.getState().isOver());
    }

    // The error files

    @Test
    void aMissingFrogIsRefused() {
        assertEquals("the 'frog' line is missing", fileErrorFor("levels/missing-frog.txt", 0));
    }

    @Test
    void anUnknownElementTypeIsRefusedWithItsLine() {
        String message = fileErrorFor("levels/unknown-type.txt", 6);

        assertContains("unknown keyword or element type 'lillypad'", message);
    }

    @Test
    void aNumberWithACommaIsRefusedWithItsLine() {
        String message = fileErrorFor("levels/bad-number.txt", 2);

        assertContains("'0,10' is not a number", message);
    }

    @Test
    void aPositionOutsideThePlayAreaIsRefusedWithItsLine() {
        String message = fileErrorFor("levels/outside.txt", 5);

        assertContains("outside the play area", message);
    }

    @Test
    void aMissingFileIsRefused() {
        assertContains("not found", fileErrorFor("levels/no-such-level.txt", 0));
    }

    // Other errors from LEVEL_FORMAT.md section 5

    @Test
    void anUnknownKeywordIsRefused() {
        assertContains("'turn'", errorFor("turn 40\n", 1));
    }

    @Test
    void tooFewOrTooManyValuesAreRefused() {
        assertContains("needs 2 or 3 values, found 1", errorFor(SETTINGS + "reed 0.2\n", 5));
        assertContains("needs 2 or 3 values, found 4", errorFor(SETTINGS + "reed 0.2 0.3 0.1 0.1\n", 5));
        assertContains("'turns' needs 1 value, found 0", errorFor("turns\n", 1));
    }

    @Test
    void turnsMustBeAWholeNumberAboveZero() {
        assertContains("not a whole number above 0", errorFor("turns 0\n", 1));
        assertContains("not a whole number above 0", errorFor("turns 2.5\n", 1));
        assertContains("not a whole number above 0", errorFor("turns -3\n", 1));
    }

    @Test
    void aSettingThatAppearsTwiceIsRefused() {
        assertContains("'turns' appears twice (first on line 1)", errorFor(SETTINGS + "turns 20\n", 5));
        assertContains("'bank far' appears twice (first on line 2)", errorFor(SETTINGS + "bank far 0.2\n", 5));
        assertContains("'frog' appears twice (first on line 4)", errorFor(SETTINGS + "frog 0.5 0.6\n", 5));
    }

    @Test
    void eachRequiredSettingMustBePresent() {
        assertEquals("the 'turns' line is missing", errorFor("bank far 0.1\nbank start 0.1\nfrog 0.5 0.62\n", 0));
        assertEquals("the 'bank far' line is missing", errorFor("turns 40\nbank start 0.1\nfrog 0.5 0.62\n", 0));
        assertEquals("the 'bank start' line is missing", errorFor("turns 40\nbank far 0.1\nfrog 0.5 0.62\n", 0));
    }

    @Test
    void badBankLinesAreRefused() {
        assertContains("unknown bank 'near'", errorFor("bank near 0.1\n", 1));
        assertContains("must be greater than 0", errorFor("bank far 0\n", 1));
    }

    @Test
    void overlappingBanksAreRefused() {
        assertContains("the two banks overlap",
                errorFor("turns 40\nbank far 0.4\nbank start 0.4\nfrog 0.5 0.62\n", 3));
    }

    @Test
    void theFrogMustStartOnTheStartBank() {
        assertContains("the frog must start on the start bank",
                errorFor("turns 40\nbank far 0.1\nbank start 0.1\nfrog 0.5 0.3\n", 4));
    }

    @Test
    void theFrogMustBeInsideThePlayArea() {
        assertContains("outside the play area", errorFor("frog 0.5 0.7\n", 1));
    }

    @Test
    void aDiameterMustBeGreaterThanZero() {
        assertContains("must be greater than 0", errorFor(SETTINGS + "reed 0.2 0.3 0\n", 5));
        assertContains("must be greater than 0", errorFor(SETTINGS + "reed 0.2 0.3 -0.1\n", 5));
    }

    @Test
    void badBagLinesAreRefused() {
        assertContains("unknown throwable kind 'rock'", errorFor("bag rock 1\n", 1));
        assertContains("unknown throwable kind 'TWIG'", errorFor("bag TWIG 1\n", 1));
        assertContains("not a whole number above 0", errorFor("bag twig 0\n", 1));
    }

    @Test
    void anIconLineAddsAThrowableIconOfThatKind() throws LevelFormatException {
        Element element = load(SETTINGS + "icon plank 0.42 0.34\n").getPond().elementsInOrder().get(0);

        ThrowableIcon icon = assertInstanceOf(ThrowableIcon.class, element);
        assertEquals(ThrowableKind.PLANK, icon.getKind());
        assertEquals(new Vec2(0.42, 0.34), icon.getPosition());
        assertEquals(0.04, icon.getSize(), DELTA);
    }

    @Test
    void anIconCanHaveItsOwnDiameter() throws LevelFormatException {
        Element icon = load(SETTINGS + "icon log 0.42 0.34 0.06\n").getPond().elementsInOrder().get(0);

        assertEquals(0.06, icon.getSize(), DELTA);
    }

    @Test
    void badIconLinesAreRefused() {
        assertContains("unknown throwable kind 'rock'", errorFor(SETTINGS + "icon rock 0.42 0.34\n", 5));
        assertContains("needs 3 or 4 values, found 2", errorFor(SETTINGS + "icon 0.42 0.34\n", 5));
        assertContains("outside the play area", errorFor(SETTINGS + "icon twig 1.5 0.34\n", 5));
        assertContains("must be greater than 0", errorFor(SETTINGS + "icon twig 0.42 0.34 0\n", 5));
    }

    @Test
    void scientificNotationAndWordsAreNotNumbers() {
        assertContains("is not a number", errorFor("frog 5e-1 0.62\n", 1));
        assertContains("is not a number", errorFor("frog NaN 0.62\n", 1));
    }

    @Test
    void rejectsNullArguments() {
        assertThrows(NullPointerException.class, () -> loader.load((String) null, Rank.RANK_1));
        assertThrows(NullPointerException.class, () -> loader.load("levels/valid.txt", null));
        assertThrows(NullPointerException.class, () -> new LevelLoader(null));
    }
}
