package ripplepond.view;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/** Builds the simple layout shared by the placeholder screens. Replaced by real content in X2. */
final class Placeholders {

    private Placeholders() {
    }

    /**
     * Returns a column with a heading and the given controls, centred in the window.
     *
     * @param heading  the screen name shown at the top
     * @param controls the buttons or other controls, in order
     * @return the screen's root node
     */
    static VBox column(String heading, Node... controls) {
        VBox box = new VBox(16);
        box.setAlignment(Pos.CENTER);
        box.getChildren().add(new Label(heading));
        box.getChildren().addAll(controls);
        return box;
    }
}