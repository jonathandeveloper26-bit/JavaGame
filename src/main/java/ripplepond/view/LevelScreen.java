package ripplepond.view;

import javafx.scene.Parent;
import javafx.scene.control.Button;

/** Placeholder level screen. S8 replaces it with the real river view and input. */
final class LevelScreen {

    private LevelScreen() {
    }

    static Parent create(FxApp app) {
        Button back = new Button("Back to title");
        back.setOnAction(event -> app.showTitle());
        return Placeholders.column("Level (placeholder)", back);
    }
}
