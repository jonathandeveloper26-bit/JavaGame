package ripplepond.model.movement;

import ripplepond.model.element.Frog;
import ripplepond.model.element.LilyPad;
import ripplepond.model.element.Placement;
import ripplepond.model.element.Reed;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.Pond;
import ripplepond.model.progress.Rank;

/**
 * Shared set-up for the movement tests.
 * <p>
 * The pond has two banks 0.125 deep: the far bank is y from 0 to 0.125, the water y
 * from 0.125 to about 0.542, and the start bank below that. The numbers are exact in
 * binary so distances land exactly on the limits.
 */
final class MoveTestSupport {

    static final double BANK = 0.125;

    /** A point on the start bank. */
    static final Vec2 ON_START_BANK = new Vec2(0.5, 0.625);

    /** A point in the water, 0.09375 above {@link #ON_START_BANK}. */
    static final Vec2 IN_WATER_NEAR_BANK = new Vec2(0.5, 0.53125);

    /** A point in the middle of the river. */
    static final Vec2 MID_RIVER = new Vec2(0.5, 0.375);

    private MoveTestSupport() {
    }

    static Frog frogAt(Vec2 position, Rank rank) {
        return new Frog(position, rank);
    }

    static Pond pondFor(Frog frog) {
        return new Pond(Pond.WIDTH, Pond.HEIGHT, BANK, BANK, frog);
    }

    /** Adds a lily pad of diameter 0.125 (radius 0.0625) to the pond. */
    static LilyPad padAt(Pond pond, Vec2 centre) {
        LilyPad pad = new LilyPad(centre, 0.125);
        pond.add(pad);
        return pad;
    }

    /** Adds a reed clump of diameter 0.03125 to the pond. */
    static Reed reedAt(Pond pond, Vec2 centre) {
        Reed reed = new Reed(centre, 0.03125);
        pond.add(reed);
        return reed;
    }

    /** Puts the frog on a new pad at its own position. */
    static LilyPad standOnNewPad(Frog frog, Pond pond) {
        LilyPad pad = padAt(pond, frog.getPosition());
        Placement.standOn(frog, pad);
        return pad;
    }
}
