package ripplepond.view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import ripplepond.controller.GameController;
import ripplepond.model.io.LevelFormatException;
import ripplepond.model.level.Lost;
import ripplepond.model.level.Won;
import ripplepond.model.progress.GameSession;

/**
 * The JavaFX application. Owns the window, the game controller and the one scene, and
 * switches screens by setting the scene's root (GDD section 5). Only this class changes
 * screens and loads levels, so the navigation rules live in one place.
 */
public class FxApp extends Application {

    /** Window size, 3:2 like the play area (GDD 2.1). */
    static final double WIDTH = 900;
    static final double HEIGHT = 600;

    private Scene scene;
    private GameController controller;
    private FxLevelView levelView;

    @Override
    public void start(Stage stage) {
        controller = new GameController(new GameSession());
        levelView = new FxLevelView(controller, this::levelFinished);
        controller.addListener(levelView);

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
        setScreen(TitleScreen.create(this));
    }

    /** Shows the How to Play screen (the How to Play button). */
    public void showHowToPlay() {
        setScreen(HowToPlayScreen.create(this));
    }

    /** Loads the first level and starts playing it (the Play button). */
    public void startGame() {
        try {
            controller.startLevel(0);
        } catch (LevelFormatException e) {
            throw new IllegalStateException("the first level could not be loaded: " + e.getMessage(), e);
        }
        showLevel();
    }

    /** Shows the level screen for the level already loaded, and starts the river animation. */
    public void showLevel() {
        levelView.refresh();
        scene.setRoot(levelView.root());
        levelView.startWaves();
    }

    /** Restarts the current level from its file (the Retry button). */
    public void retryLevel() {
        try {
            controller.reset();
        } catch (LevelFormatException e) {
            throw new IllegalStateException("the level could not be reloaded: " + e.getMessage(), e);
        }
        showLevel();
    }

    /** Whether a level follows the one just won. */
    public boolean hasNextLevel() {
        return controller.hasNextLevel();
    }

    /** Starts the level after the one just won (the Next level button). */
    public void playNextLevel() {
        try {
            controller.nextLevel();
        } catch (LevelFormatException e) {
            throw new IllegalStateException("the next level could not be loaded: " + e.getMessage(), e);
        }
        showLevel();
    }

    /** Shows the level complete screen, with the summary of the level just won. */
    public void showLevelComplete() {
        setScreen(LevelCompleteScreen.create(this, LevelSummary.of(controller)));
    }

    /** Shows the game over screen. */
    public void showGameOver() {
        setScreen(GameOverScreen.create(this));
    }

    /** Sets a screen that is not the level, and stops the river animation. */
    private void setScreen(Parent screen) {
        levelView.stopWaves();
        scene.setRoot(screen);
    }

    /** Closes the game. Used by Quit and Esc. */
    public void quit() {
        Platform.exit();
    }

    /** Moves to the right screen once a turn has won or lost the level. */
    private void levelFinished() {
        if (controller.getLevel().getState() instanceof Won) {
            showLevelComplete();
        } else if (controller.getLevel().getState() instanceof Lost) {
            showGameOver();
        }
    }
}