package ripplepond.view;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link HowToPlayText}: the How to Play screen shows the real X3 text.
 */
class HowToPlayTextTest {

    @Test
    void loadsTheHowToPlayTextFromTheResources() {
        String text = HowToPlayText.load();

        assertNotEquals(HowToPlayText.MISSING, text);
        assertTrue(text.startsWith("RIPPLE POND - HOW TO PLAY"), text);
        assertTrue(text.contains("Win: reach the far bank."), text);
    }

    @Test
    void theTextHasNoTrailingBlankLine() {
        String text = HowToPlayText.load();

        assertTrue(!text.endsWith("\n") && !text.endsWith("\r"));
    }
}
