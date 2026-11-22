package ripplepond.model.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import ripplepond.model.element.Element;
import ripplepond.model.element.Reed;
import ripplepond.model.geometry.Vec2;

class ElementOrderTest {

    private static Reed at(double x, double y) {
        return new Reed(new Vec2(x, y), 0.05);
    }

    @Test
    void sortsTopToBottomThenLeftToRight() {
        Reed topLeft = at(0.2, 0.1);
        Reed topRight = at(0.8, 0.1);
        Reed bottom = at(0.5, 0.6);

        List<Element> sorted = ElementOrder.sorted(Arrays.asList(bottom, topRight, topLeft));

        assertEquals(List.of(topLeft, topRight, bottom), sorted);
    }

    @Test
    void equalCentresKeepInsertionOrder() {
        Reed first = at(0.5, 0.3);
        Reed second = at(0.5, 0.3);

        assertSame(first, ElementOrder.sorted(Arrays.asList(first, second)).get(0));
        assertSame(second, ElementOrder.sorted(Arrays.asList(second, first)).get(0));
    }

    @Test
    void sameElementsInAnyInputOrderGiveTheSameResult() {
        Reed a = at(0.1, 0.2);
        Reed b = at(0.9, 0.2);
        Reed c = at(0.5, 0.5);

        assertEquals(ElementOrder.sorted(List.of(a, b, c)), ElementOrder.sorted(List.of(c, b, a)));
    }

    @Test
    void doesNotChangeTheInputList() {
        Reed far = at(0.9, 0.9);
        Reed near = at(0.1, 0.1);
        List<Element> input = new ArrayList<>(List.of(far, near));

        ElementOrder.sorted(input);

        assertSame(far, input.get(0));
        assertSame(near, input.get(1));
    }
}
