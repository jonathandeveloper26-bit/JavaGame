package ripplepond.model.level;

import ripplepond.model.Balance;
import ripplepond.model.element.Frog;
import ripplepond.model.geometry.Vec2;

/**
 * A pond for tests in which every point is in the area the test chooses. The level
 * tests use it to put the frog on a bank or in the water without depending on the real
 * {@code Pond.areaAt} (S3).
 */
final class FakePond extends Pond {

    /** The area every point is in. Tests set it directly. */
    AreaType area = AreaType.WATER;

    FakePond(Frog frog) {
        super(Balance.PLAY_AREA_WIDTH, Balance.PLAY_AREA_HEIGHT, frog);
    }

    @Override
    public AreaType areaAt(Vec2 point) {
        return area;
    }
}
