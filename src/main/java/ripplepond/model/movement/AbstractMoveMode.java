package ripplepond.model.movement;

import java.util.Objects;

import ripplepond.model.Balance;
import ripplepond.model.element.Frog;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.AreaType;
import ripplepond.model.level.Pond;

/**
 * What every way of moving has in common.
 * <p>
 * {@link #isLegal} is a Template method. It checks the rules shared by every mode, in a
 * fixed order: the move goes somewhere, no further than the mode allows, and stays
 * inside the play area. Only then does it ask the subclass's own rule, {@link #allows}.
 * The cost is the same formula for every mode: energy per unit times the number of
 * units travelled.
 */
public abstract class AbstractMoveMode implements MoveMode {

    /**
     * How far past its range a move may be and still count as in range. Distances are
     * computed with decimals, so a hop meant to be exactly 0.10 can come out as
     * 0.10000000000000003; this keeps such a move legal. It is not a game rule.
     */
    private static final double ROUNDING = 1e-9;

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code distance} is negative or not a number
     */
    @Override
    public final double costFor(double distance) {
        if (!(distance >= 0)) {
            throw new IllegalArgumentException("distance must be 0 or more: " + distance);
        }
        return energyPerUnit() * distance / Balance.MOVE_UNIT;
    }

    /**
     * {@inheritDoc}
     * <p>
     * A move is refused if the target is where the frog already is, further than
     * {@link #maxDistance}, or outside the play area. Otherwise the mode's own rule
     * decides.
     *
     * @throws NullPointerException if any argument is {@code null}
     */
    @Override
    public final boolean isLegal(Frog frog, Vec2 target, Pond pond) {
        Objects.requireNonNull(frog, "frog");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(pond, "pond");
        double distance = frog.getPosition().distanceTo(target);
        if (distance == 0 || distance > maxDistance(frog) + ROUNDING) {
            return false;
        }
        if (!insidePlayArea(target, pond)) {
            return false;
        }
        return allows(frog, target, pond);
    }

    /**
     * The rule that belongs to this mode only. It is asked only for a target that is in
     * range and inside the play area.
     *
     * @param frog   the frog that would move
     * @param target where the frog would end up
     * @param pond   the pond the frog is in
     * @return whether this mode allows the move
     */
    protected abstract boolean allows(Frog frog, Vec2 target, Pond pond);

    /**
     * Returns {@code true} if a point is on either bank.
     *
     * @param point the point, inside the play area
     * @param pond  the pond
     * @return whether the point is on the start bank or the far bank
     */
    protected static boolean onBank(Vec2 point, Pond pond) {
        AreaType area = pond.areaAt(point);
        return area == AreaType.START_BANK || area == AreaType.FAR_BANK;
    }

    /**
     * Returns {@code true} if the frog stands on something solid: a bank, or a floating
     * object.
     *
     * @param frog the frog
     * @param pond the pond
     * @return whether the frog is on a surface rather than in the water
     */
    protected static boolean onSurface(Frog frog, Pond pond) {
        return frog.standingOn().isPresent() || onBank(frog.getPosition(), pond);
    }

    private static boolean insidePlayArea(Vec2 point, Pond pond) {
        return point.getX() >= 0 && point.getX() <= pond.getWidth()
                && point.getY() >= 0 && point.getY() <= pond.getHeight();
    }
}
