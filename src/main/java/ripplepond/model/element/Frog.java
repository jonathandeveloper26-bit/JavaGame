package ripplepond.model.element;

import java.util.Objects;
import java.util.Optional;

import ripplepond.model.Balance;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.Pond;
import ripplepond.model.movement.MoveMode;
import ripplepond.model.progress.Rank;

/**
 * The frog the player guides across the river (GDD section 4).
 * <p>
 * The frog has health and energy (GDD section 4.1). Both start full, at the maximum for
 * the frog's rank, and only change through the methods below, which keep them between 0
 * and that maximum. Health is a whole number of HP. Energy is a decimal number, because
 * moves cost energy by distance (GDD section 4.2).
 * <p>
 * The frog moves with {@link #move}, using a {@link MoveMode}: walk, hop or swim. The
 * mode knows the rules; the frog pays the energy and keeps track of what it stands on.
 * <p>
 * A new frog is created for each level, so health and energy are full at the start of
 * every level.
 */
public final class Frog extends AbstractElement {

    /**
     * How much energy may be missing and the move still count as paid for. Costs are
     * computed with decimals, so a move meant to cost exactly the energy left can come
     * out a hair above it. It is not a game rule.
     */
    private static final double ROUNDING = 1e-9;

    private final Rank rank;
    private int health;
    private double energy;
    private FloatingObject standingOn;

    /**
     * Creates a frog with full health and energy for its rank. Its size is
     * {@link Balance#FROG_SIZE}.
     *
     * @param position the centre of the frog, in play-area widths
     * @param rank     the frog's rank, which sets its maximum health and energy
     * @throws NullPointerException if {@code position} or {@code rank} is {@code null}
     */
    public Frog(Vec2 position, Rank rank) {
        super(position, Balance.FROG_SIZE);
        this.rank = Objects.requireNonNull(rank, "rank");
        this.health = rank.maxHealth();
        this.energy = rank.maxEnergy();
    }

    /**
     * Returns the frog's rank.
     *
     * @return the rank
     */
    public Rank getRank() {
        return rank;
    }

    /**
     * Returns the frog's current health.
     *
     * @return the health, from 0 to the maximum for the rank
     */
    public int getHealth() {
        return health;
    }

    /**
     * Returns the frog's current energy.
     *
     * @return the energy, from 0 to the maximum for the rank
     */
    public double getEnergy() {
        return energy;
    }

    /**
     * Takes away health, for example when a predator attacks. Health never drops below 0.
     *
     * @param amount the health to take away; 0 or more
     * @throws IllegalArgumentException if {@code amount} is negative
     */
    public void takeDamage(int amount) {
        requireNotNegative(amount);
        health = Math.max(0, health - amount);
    }

    /**
     * Gives back health, for example when the frog eats. Health never rises above the
     * maximum for the frog's rank.
     *
     * @param amount the health to give back; 0 or more
     * @throws IllegalArgumentException if {@code amount} is negative
     */
    public void restoreHealth(int amount) {
        requireNotNegative(amount);
        // Added as a long so that a huge amount cannot overflow past the maximum.
        health = (int) Math.min(rank.maxHealth(), (long) health + amount);
    }

    /**
     * Uses up energy, for example when the frog moves. Energy never drops below 0.
     *
     * @param amount the energy to use; 0 or more
     * @throws IllegalArgumentException if {@code amount} is negative or not a number
     */
    public void spendEnergy(double amount) {
        requireNotNegative(amount);
        energy = Math.max(0, energy - amount);
    }

    /**
     * Gives back energy, for example when the frog eats. Energy never rises above the
     * maximum for the frog's rank.
     *
     * @param amount the energy to give back; 0 or more
     * @throws IllegalArgumentException if {@code amount} is negative or not a number
     */
    public void restoreEnergy(double amount) {
        requireNotNegative(amount);
        energy = Math.min(rank.maxEnergy(), energy + amount);
    }

    /**
     * Returns {@code true} while the frog has health left. At 0 health the level is lost.
     *
     * @return whether health is above 0
     */
    public boolean isAlive() {
        return health > 0;
    }

    /**
     * Returns {@code true} while the frog has energy left. At 0 energy the level is lost.
     *
     * @return whether energy is above 0
     */
    public boolean hasEnergy() {
        return energy > 0;
    }

    /**
     * Returns the floating object the frog is standing on, if any. It is empty when the
     * frog is on a bank.
     *
     * @return the object under the frog, or an empty {@code Optional}
     */
    public Optional<FloatingObject> standingOn() {
        return Optional.ofNullable(standingOn);
    }

    /**
     * Returns {@code true} if the frog has enough energy for a move. Exactly enough
     * counts, so the frog can use its last energy to reach the far bank.
     *
     * @param mode   how the frog would move
     * @param target where the frog would end up
     * @return whether the frog can pay for the move
     * @throws NullPointerException if {@code mode} or {@code target} is {@code null}
     */
    public boolean canAfford(MoveMode mode, Vec2 target) {
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(target, "target");
        return energy >= costOf(mode, target) - ROUNDING;
    }

    /**
     * Moves the frog, if the move is legal and the frog can pay for it (GDD 4.2).
     * <p>
     * A refused move changes nothing. Otherwise the frog pays the energy, moves to the
     * target, and from then on stands on the floating object under the target, or on
     * nothing when the target is a bank or open water. A move paid with exactly the
     * energy left leaves the frog with none.
     *
     * @param mode   how the frog moves: walk, hop or swim
     * @param target where the frog ends up, in play-area widths
     * @param pond   the pond the frog is in
     * @return {@code true} if the frog moved; {@code false} if the move was refused
     * @throws NullPointerException if any argument is {@code null}
     */
    public boolean move(MoveMode mode, Vec2 target, Pond pond) {
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(pond, "pond");
        if (!mode.isLegal(this, target, pond) || !canAfford(mode, target)) {
            return false;
        }
        double cost = costOf(mode, target);
        // Paying "exactly" the energy left must leave 0, not a rounding crumb.
        spendEnergy(energy - cost < ROUNDING ? energy : cost);
        moveTo(target);
        standOn(pond.surfaceAt(target).orElse(null));
        return true;
    }

    private double costOf(MoveMode mode, Vec2 target) {
        return mode.costFor(getPosition().distanceTo(target));
    }

    /**
     * Puts the frog on a floating object, or takes it off the one it is on.
     * <p>
     * This is the only place the link between the frog and its object changes. It
     * updates both sides at once: the old object loses its rider and the new one gains
     * it. The frog does not move; the caller places it first. Only code in this package
     * calls it: moving the frog (J6) and the tests.
     *
     * @param surface the object the frog now stands on, or {@code null} for none
     * @throws IllegalArgumentException if {@code surface} does not cover the frog's
     *                                  centre
     */
    void standOn(FloatingObject surface) {
        if (surface != null && !surface.covers(getPosition())) {
            throw new IllegalArgumentException("the frog is not on that object");
        }
        if (standingOn != null) {
            standingOn.setRider(null);
        }
        standingOn = surface;
        if (surface != null) {
            surface.setRider(this);
        }
    }

    private static void requireNotNegative(double amount) {
        // Written as "not 0 or more" so that NaN is rejected too.
        if (!(amount >= 0)) {
            throw new IllegalArgumentException("amount must be 0 or more: " + amount);
        }
    }
}
