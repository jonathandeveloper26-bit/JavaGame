package ripplepond.model.progress;

import java.util.Objects;

import ripplepond.model.Balance;
import ripplepond.model.element.Frog;

/**
 * How much experience a completed level is worth (GDD section 4.3).
 * <p>
 * XP for a level = 50 + 2 x health left + 1 x energy left. The fixed part rewards
 * finishing; the rest rewards finishing well, so a frog that kept its health and energy
 * grows faster. The three numbers are in {@link Balance}.
 */
public class ExperienceRules {

    /**
     * Returns the XP a frog earns for the level it has just completed.
     * <p>
     * Energy is a decimal number, so only whole points of energy count: 7.9 energy left
     * is worth 7 XP.
     *
     * @param frog the frog at the end of the level
     * @return the XP earned, at least {@link Balance#XP_PER_LEVEL}
     * @throws NullPointerException if {@code frog} is {@code null}
     */
    public int xpFor(Frog frog) {
        Objects.requireNonNull(frog, "frog");
        int wholeEnergy = (int) Math.floor(frog.getEnergy());
        return Balance.XP_PER_LEVEL
                + Balance.XP_PER_HEALTH_LEFT * frog.getHealth()
                + Balance.XP_PER_ENERGY_LEFT * wholeEnergy;
    }
}
