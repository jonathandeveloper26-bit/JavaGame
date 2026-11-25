package ripplepond.view;

import javafx.scene.Parent;
import javafx.scene.control.Button;

/** Placeholder level complete screen. Xaryab replaces it in X2. */
final class LevelCompleteScreen {

    private LevelCompleteScreen() {
    }

    static Parent create(FxApp app) {
        Button next = new Button("Next level");
        next.setOnAction(event -> app.playNextLevel());
        next.setDisable(!app.hasNextLevel());
        Button title = new Button("Back to title");
        title.setOnAction(event -> app.showTitle());
        return Placeholders.column("Level complete (placeholder)", next, title);
    }
}
