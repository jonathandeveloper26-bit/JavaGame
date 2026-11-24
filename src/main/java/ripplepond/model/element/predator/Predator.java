package ripplepond.model.element.predator;

import java.util.Objects;

import ripplepond.model.Balance;
import ripplepond.model.element.AbstractElement;
import ripplepond.model.element.Frog;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.Pond;

/**
 * An animal that hunts the frog (GDD 2.4).
 * <p>
 * Template method: {@link #act} is the fixed turn for every predator. It moves on every
 * second turn, then attacks on every turn if the frog is in reach. Subclasses fill in only
 * what differs: how much damage they deal and where they may go.
 * <p>
 * Predators ignore ripples, so the inherited {@code onRipple} does nothing.
 */
public abstract class Predator extends AbstractElement {

    /** If a full step is blocked, the predator tries a half step, then stays put. */
    private static final double[] STEP_FRACTIONS = {1.0, 0.5};

    private final ChaseStrategy chase;

    /**
     * Creates a predator.
     *
     * @param position the centre, in play-area widths
     * @param size     the diameter, in play-area widths; greater than 0
     * @param chase    how the predator moves towards the frog
     * @throws NullPointerException     if {@code position} or {@code chase} is {@code null}
     * @throws IllegalArgumentException if {@code size} is not greater than 0
     */
    protected Predator(Vec2 position, double size, ChaseStrategy chase) {
        super(position, size);
        this.chase = Objects.requireNonNull(chase, "chase");
    }

    /**
     * Takes this predator's turn: on even turns it moves towards the frog, then, on every
     * turn, it attacks if the frog is in reach.
     *
     * @param pond the pond the predator is in
     * @param turn the turn number being played, starting at 1
     * @return the health the frog lost to this predator this turn; 0 if none
     * @throws NullPointerException if {@code pond} is {@code null}
     */
    public final int act(Pond pond, int turn) {
        Objects.requireNonNull(pond, "pond");
        if (turn % Balance.PREDATOR_MOVE_EVERY == 0) {
            chaseFrog(pond);
        }
        return attack(pond.getFrog());
    }

    /** Returns the health this predator takes from the frog when it attacks. */
    public abstract int getDamage();

    /**
     * Returns {@code true} if this predator may stand with its centre at {@code centre}.
     * Only called for points inside the play area.
     *
     * @param pond   the pond
     * @param centre the proposed centre
     * @return whether the predator may be there
     */
    protected abstract boolean canEnter(Pond pond, Vec2 centre);

    /**
     * Returns {@code true} if this predator may stand with its centre at {@code centre}
     * and the whole circle is inside the play area.
     */
    boolean isLegalAt(Pond pond, Vec2 centre) {
        double r = getRadius();
        if (centre.getX() - r < 0 || centre.getX() + r > pond.getWidth()) {
            return false;
        }
        if (centre.getY() - r < 0 || centre.getY() + r > pond.getHeight()) {
            return false;
        }
        return canEnter(pond, centre);
    }

    private void chaseFrog(Pond pond) {
        Frog frog = pond.getFrog();
        double stopDistance = getRadius() + frog.getRadius();
        if (distanceTo(frog) <= stopDistance) {
            return; // already touching: close enough, so do not move onto the frog
        }
        // Aim for the point just outside the frog, on the side this predator is on.
        Vec2 towardsUs = getPosition().minus(frog.getPosition()).normalised();
        Vec2 goal = frog.getPosition().plus(towardsUs.scale(stopDistance));
        for (double fraction : STEP_FRACTIONS) {
            Vec2 candidate = chase.nextPosition(getPosition(), goal, Balance.PREDATOR_STEP * fraction);
            if (isLegalAt(pond, candidate)) {
                moveTo(candidate);
                return;
            }
        }
    }

    private int attack(Frog frog) {
        if (!touches(frog)) {
            return 0;
        }
        frog.takeDamage(getDamage());
        return getDamage();
    }
}