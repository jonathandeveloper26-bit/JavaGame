package ripplepond.view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * The JavaFX application. Owns the window and the one scene in it, and switches
 * screens by swapping the scene's root. Only this class changes screens, so the
 * navigation rules live in one place (GDD section 5).
 */
public class FxApp extends Application {

    /** Window size, 3:2 like the play area (GDD 2.1). */
    static final double WIDTH = 900;
    static final double HEIGHT = 600;

    private Scene scene;

    @Override
    public void start(Stage stage) {
        scene = new Scene(new Pane(), WIDTH, HEIGHT);
        // Registered once on the scene, so Esc works on every screen.
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                quit();
            }
        });
        stage.setTitle("Ripple Pond");
        stage.setScene(scene);
        stage.show();
        showTitle();
    }

    /** Shows the title screen. */
    public void showTitle() {
        scene.setRoot(TitleScreen.create(this));
    }

    /** Shows the level screen. S8 replaces the placeholder with the real level view. */
    public void showLevel() {
        scene.setRoot(LevelScreen.create(this));
    }

    /** Shows the level complete screen. */
    public void showLevelComplete() {
        scene.setRoot(LevelCompleteScreen.create(this));
    }

    /** Shows the game over screen. */
    public void showGameOver() {
        scene.setRoot(GameOverScreen.create(this));
    }

    /** Closes the game. Used by Quit and Esc. */
    public void quit() {
        Platform.exit();
    }
}