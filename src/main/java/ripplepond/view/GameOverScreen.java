package ripplepond.view;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * The game over screen for a lost level (GDD section 5): why it was lost, Retry and Back
 * to title. The facts come from {@link LossSummary}; this class only lays them out.
 */
final class GameOverScreen {

    private GameOverScreen() {
    }

    /**
     * Builds the screen.
     *
     * @param app     the application, which the buttons call
     * @param summary what to show about the level just lost
     * @return the screen's root node
     */
    static Parent create(FxApp app, LossSummary summary) {
        List<Node> content = new ArrayList<>();
        for (String line : summary.lines()) {
            content.add(new Label(line));
        }

        Button retry = new Button("Retry");
        retry.setOnAction(event -> app.retryLevel());
        Button title = new Button("Back to title");
        title.setOnAction(event -> app.showTitle());
        content.add(retry);
        content.add(title);

        return Placeholders.column("Game over", content.toArray(Node[]::new));
    }
}
