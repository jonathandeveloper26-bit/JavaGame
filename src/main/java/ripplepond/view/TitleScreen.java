package ripplepond.view;

import javafx.scene.Parent;
import javafx.scene.control.Button;

/** The title screen: Play, How to Play and Quit (GDD section 5). Placeholder until X2. */
final class TitleScreen {

    private TitleScreen() {
    }

    static Parent create(FxApp app) {
        return Placeholders.column("Ripple Pond",
                button("Play", app::showLevel),
                button("How to Play", () -> { /* X2 and X3 */ }),
                button("Quit", app::quit));
    }

    private static Button button(String text, Runnable action) {
        Button button = new Button(text);
        button.setOnAction(event -> action.run());
        return button;
    }
}