package ripplepond.view;

import javafx.application.Application;

/**
 * Starts the game. Kept separate from FxApp so JavaFX can start
 * when the app runs from a plain jar without module setup.
 */
public class Launcher {

    public static void main(String[] args) {
        Application.launch(FxApp.class, args);
    }
}
