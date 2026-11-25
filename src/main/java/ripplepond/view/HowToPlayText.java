package ripplepond.view;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Reads the How to Play text (task X3) from the game's resources, for the How to Play
 * screen. Kept apart from the screen so it can be tested without JavaFX.
 */
final class HowToPlayText {

    /** Where the text lives in the resources. */
    static final String RESOURCE = "how-to-play.txt";

    /** Shown instead if the file cannot be read, so the screen still opens. */
    static final String MISSING = "The How to Play text could not be found.";

    private HowToPlayText() {
    }

    /**
     * Returns the How to Play text.
     *
     * @return the text of {@code how-to-play.txt}, or {@link #MISSING} if it cannot be read
     */
    static String load() {
        try (InputStream in = HowToPlayText.class.getClassLoader().getResourceAsStream(RESOURCE)) {
            if (in == null) {
                return MISSING;
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).strip();
        } catch (IOException e) {
            return MISSING;
        }
    }
}
