package ripplepond;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Checks {@code how-to-play.txt} (task X3): it is there for the title screen to show,
 * stays within 15 short lines, and covers the goal, the turn, the three moves and how to
 * win and lose.
 */
class HowToPlayTextTest {

    private static List<String> lines() throws IOException {
        try (InputStream in = HowToPlayTextTest.class.getClassLoader().getResourceAsStream("how-to-play.txt")) {
            assertNotNull(in, "how-to-play.txt is missing from the resources");
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).lines().toList();
        }
    }

    @Test
    void theTextIsAtMostFifteenShortLines() throws IOException {
        List<String> lines = lines();

        assertFalse(lines.isEmpty());
        assertTrue(lines.size() <= 15, "lines: " + lines.size());
        for (String line : lines) {
            assertTrue(line.length() <= 90, "too long: " + line);
        }
    }

    @Test
    void theTextCoversWhatTheTaskAsksFor() throws IOException {
        String text = String.join("\n", lines()).toLowerCase();

        for (String topic : List.of("far bank", "each turn", "walk", "hop", "swim", "win:", "lose:")) {
            assertTrue(text.contains(topic), "missing: " + topic);
        }
    }
}
