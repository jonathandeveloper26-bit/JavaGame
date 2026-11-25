package ripplepond.view;

import java.util.Map;

import javafx.scene.control.Button;
import ripplepond.model.element.ThrowableKind;
import ripplepond.model.level.ThrowBag;

/** Shows how many of each object the bag holds, and disables the kinds it has none of. */
final class ThrowBagView {

    private ThrowBagView() {
    }

    static void update(ThrowBag bag, Map<ThrowableKind, Button> buttons) {
        for (Map.Entry<ThrowableKind, Button> entry : buttons.entrySet()) {
            ThrowableKind kind = entry.getKey();
            Button button = entry.getValue();
            int count = bag.count(kind);
            button.setText(name(kind) + " x" + count);
            button.setDisable(count == 0);
        }
    }

    private static String name(ThrowableKind kind) {
        String lower = kind.name().toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
