package ripplepond.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * The JavaFX application. Opens the main window and switches screens.
 */
public class FxApp extends Application {

    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(new Pane(), 900, 600); // 3:2 ratio
        stage.setTitle("Ripple Pond");
        stage.setScene(scene);
        stage.show();
    }
}