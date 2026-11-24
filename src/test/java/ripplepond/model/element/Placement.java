package ripplepond.model.element;

/**
 * Test-only helper for tests in other packages: puts a frog on a floating object.
 * {@code Frog.standOn} is package-private so that only moves change what the frog
 * stands on; this class lives in the same package, in the test sources only, so the
 * game code cannot use it.
 */
public final class Placement {

    private Placement() {
    }

    /**
     * Puts the frog on an object that covers its centre.
     *
     * @param frog   the frog
     * @param object the object under the frog
     */
    public static void standOn(Frog frog, FloatingObject object) {
        frog.standOn(object);
    }
}
