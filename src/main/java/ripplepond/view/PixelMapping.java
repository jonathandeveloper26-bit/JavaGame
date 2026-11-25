package ripplepond.view;

import java.util.Objects;

import ripplepond.model.Balance;
import ripplepond.model.geometry.Vec2;

/**
 * Converts between play-area units and window pixels (GDD section 2.1).
 * <p>
 * The play area keeps its 3:2 shape. It is scaled by one factor on both axes, so a
 * distance means the same thing across and down, and it is centred with margins when the
 * window has another shape. This class has no JavaFX imports, so it is unit tested
 * without a display.
 */
final class PixelMapping {

    /** A point in window pixels. */
    record Point(double x, double y) {
    }

    private final double scale;
    private final double offsetX;
    private final double offsetY;

    /**
     * Fits the play area into a window of the given size.
     *
     * @param windowWidth  the width of the drawing area in pixels, greater than 0
     * @param windowHeight the height of the drawing area in pixels, greater than 0
     * @throws IllegalArgumentException if either size is not greater than 0
     */
    PixelMapping(double windowWidth, double windowHeight) {
        if (!(windowWidth > 0 && windowHeight > 0)) {
            throw new IllegalArgumentException("window size must be greater than 0");
        }
        scale = Math.min(windowWidth / Balance.PLAY_AREA_WIDTH,
                windowHeight / Balance.PLAY_AREA_HEIGHT);
        offsetX = (windowWidth - Balance.PLAY_AREA_WIDTH * scale) / 2;
        offsetY = (windowHeight - Balance.PLAY_AREA_HEIGHT * scale) / 2;
    }

    /** Returns the pixels per play-area unit, the same on both axes. */
    double scale() {
        return scale;
    }

    /**
     * Converts a play-area point to window pixels.
     *
     * @param point the point, in play-area units
     * @return the same point in pixels
     */
    Point toPixels(Vec2 point) {
        Objects.requireNonNull(point, "point");
        return new Point(offsetX + point.getX() * scale, offsetY + point.getY() * scale);
    }

    /**
     * Converts window pixels to a play-area point.
     *
     * @param x the horizontal pixel
     * @param y the vertical pixel
     * @return the point, in play-area units; may lie outside the play area
     */
    Vec2 toFraction(double x, double y) {
        return new Vec2((x - offsetX) / scale, (y - offsetY) / scale);
    }

    /**
     * Returns {@code true} if a pixel lies inside the play area, not in the margins.
     *
     * @param x the horizontal pixel
     * @param y the vertical pixel
     * @return whether the pixel is on the play area
     */
    boolean inPlayArea(double x, double y) {
        Vec2 p = toFraction(x, y);
        return p.getX() >= 0 && p.getX() <= Balance.PLAY_AREA_WIDTH
                && p.getY() >= 0 && p.getY() <= Balance.PLAY_AREA_HEIGHT;
    }
}
