package ripplepond.view;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * The level complete screen (GDD section 5): turns used, health and energy left, XP
 * gained, the new rank if there is one, and Next level. The facts come from
 * {@link LevelSummary}; this class only lays them out.
 */
final class LevelCompleteScreen {

    private LevelCompleteScreen() {
    }

    /**
     * Builds the screen.
     *
     * @param app     the application, which the buttons call
     * @param summary what to show about the level just won
     * @return the screen's root node
     */
    static Parent create(FxApp app, LevelSummary summary) {
        List<Node> content = new ArrayList<>();
        for (String line : summary.lines()) {
            content.add(new Label(line));
        }

        Button next = new Button("Next level");
        next.setOnAction(event -> app.playNextLevel());
        // On the last level there is nothing to go on to.
        next.setDisable(!app.hasNextLevel());
        Button title = new Button("Back to title");
        title.setOnAction(event -> app.showTitle());
        content.add(next);
        content.add(title);

        return Placeholders.column("Level complete", content.toArray(Node[]::new));
    }
}
