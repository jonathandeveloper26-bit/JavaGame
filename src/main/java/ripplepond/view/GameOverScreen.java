package ripplepond.view;

import javafx.scene.Parent;
import javafx.scene.control.Button;

/** Placeholder game over screen. Xaryab replaces it in X2. */
final class GameOverScreen {

    private GameOverScreen() {
    }

    static Parent create(FxApp app) {
        Button retry = new Button("Retry");
        retry.setOnAction(event -> app.retryLevel());
        Button title = new Button("Back to title");
        title.setOnAction(event -> app.showTitle());
        return Placeholders.column("Game over (placeholder)", retry, title);
    }
}
