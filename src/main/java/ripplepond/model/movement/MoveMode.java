package ripplepond.model.movement;

import ripplepond.model.element.Frog;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.Pond;

/**
 * A way for the frog to move: walk, hop or swim (GDD section 4.2).
 * <p>
 * This is the Strategy pattern. The frog does not know the rules of each kind of move;
 * it asks the chosen mode whether a move is legal and what it costs. Adding a new way to
 * move means adding a class, not changing the frog.
 */
public interface MoveMode {

    /**
     * Returns the energy this mode costs per {@code Balance.MOVE_UNIT} travelled.
     *
     * @return the energy per unit of distance
     */
    double energyPerUnit();

    /**
     * Returns the furthest a frog can go in one move of this mode.
     *
     * @param frog the frog that would move; its rank can matter
     * @return the furthest distance, in play-area widths
     */
    double maxDistance(Frog frog);

    /**
     * Returns {@code true} if the frog may move to the target with this mode, ignoring
     * whether it can pay for the move.
     *
     * @param frog   the frog that would move
     * @param target where the frog would end up, in play-area widths
     * @param pond   the pond the frog is in
     * @return whether the move follows this mode's rules
     */
    boolean isLegal(Frog frog, Vec2 target, Pond pond);

    /**
     * Returns the energy a move of this mode costs over a distance. The cost grows in
     * proportion to the distance.
     *
     * @param distance how far the frog moves, in play-area widths; 0 or more
     * @return the energy the move costs
     */
    double costFor(double distance);
}
