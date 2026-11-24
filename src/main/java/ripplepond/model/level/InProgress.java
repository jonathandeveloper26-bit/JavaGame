package ripplepond.model.level;

import java.util.Objects;

/**
 * The state of a level that is still being played: the only state that plays turns.
 * <p>
 * Each turn it runs the {@link TurnEngine}, which carries out the five steps of the turn,
 * and then asks the {@link EndRules} what state the level is in afterwards. It returns
 * itself while the level goes on, and {@link Won} or {@link Lost} once it ends.
 */
public final class InProgress implements LevelState {

    private final TurnEngine engine;
    private final EndRules rules;

    /**
     * Creates the state a level starts in, with the game's turn engine and end rules.
     */
    public InProgress() {
        this(new TurnEngine(), new EndRules());
    }

    /**
     * Creates the state with a chosen turn engine and end rules. Tests use this to run
     * the state with a stand-in engine.
     *
     * @param engine the engine that runs each turn
     * @param rules  the rules that decide the state after each turn
     * @throws NullPointerException if {@code engine} or {@code rules} is {@code null}
     */
    public InProgress(TurnEngine engine, EndRules rules) {
        this.engine = Objects.requireNonNull(engine, "engine");
        this.rules = Objects.requireNonNull(rules, "rules");
    }

    /**
     * Plays one turn: runs the turn engine, then returns the state the end rules choose.
     *
     * @param level  the level being played
     * @param action what the player asked for this turn
     * @return this state if the level goes on; {@link Won} or {@link Lost} if it ended
     * @throws NullPointerException if {@code level} or {@code action} is {@code null}
     */
    @Override
    public LevelState playTurn(Level level, TurnAction action) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(action, "action");
        engine.runTurn(level, action);
        return rules.evaluate(level);
    }

    /**
     * Returns {@code false}: a level in progress still accepts turns.
     *
     * @return {@code false}
     */
    @Override
    public boolean isOver() {
        return false;
    }

    @Override
    public String toString() {
        return "InProgress";
    }
}
