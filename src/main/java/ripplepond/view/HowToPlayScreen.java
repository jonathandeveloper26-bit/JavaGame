package ripplepond.view;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * The How to Play screen (GDD section 5): the rules in a few lines, and a way back to
 * the title. The text comes from {@code how-to-play.txt} (task X3).
 */
final class HowToPlayScreen {

    private HowToPlayScreen() {
    }

    /**
     * Builds the screen.
     *
     * @param app the application, for the Back to title button
     * @return the screen's root node
     */
    static Parent create(FxApp app) {
        Label text = new Label(HowToPlayText.load());
        // Monospaced, so the numbered steps of a turn line up.
        text.setStyle("-fx-font-family: monospace; -fx-font-size: 13px;");
        text.setWrapText(true);

        Button back = new Button("Back to title");
        back.setOnAction(event -> app.showTitle());

        return Placeholders.column("How to Play", text, back);
    }
}
