package ripplepond.view;

import javafx.scene.Parent;
import javafx.scene.control.Button;

/** The title screen: Play, How to Play and Quit (GDD section 5). */
final class TitleScreen {

    private TitleScreen() {
    }

    /**
     * Builds the screen.
     *
     * @param app the application, which each button calls
     * @return the screen's root node
     */
    static Parent create(FxApp app) {
        return Placeholders.column("Ripple Pond",
                button("Play", app::startGame),
                button("How to Play", app::showHowToPlay),
                button("Quit", app::quit));
    }

    private static Button button(String text, Runnable action) {
        Button button = new Button(text);
        button.setOnAction(event -> action.run());
        return button;
    }
}