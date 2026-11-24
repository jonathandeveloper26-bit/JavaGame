package ripplepond.model.level;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ripplepond.model.event.GameEvent;
import ripplepond.model.event.PondListener;

/**
 * One playable level: the pond, the throw bag, the turn count and the current state.
 */
public class Level {

    private final Pond pond;
    private final ThrowBag bag;
    private final int turnLimit;
    private LevelState state;
    private int turn;
    private final List<PondListener> listeners = new ArrayList<>();
    private final TurnEngine engine = new TurnEngine();

    /**
     * Creates a level at turn 0.
     *
     * @param pond      the pond the level is played in
     * @param bag       the objects the player can throw
     * @param turnLimit the number of turns allowed
     * @param state     the state the level starts in
     */
    public Level(Pond pond, ThrowBag bag, int turnLimit, LevelState state) {
        this.pond = Objects.requireNonNull(pond, "pond");
        this.bag = Objects.requireNonNull(bag, "bag");
        this.turnLimit = turnLimit;
        this.state = state;
        this.turn = 0;
    }

    /**
     * Plays one turn. The turn counter goes up by one before the engine runs.
     * <p>
     * TODO(J3): once {@code LevelState.playTurn} exists, replace the direct engine call with
     * the state delegation below, which lets a won or lost level refuse turns.
     * <pre>
     * // state = state.playTurn(this, action);
     * </pre>
     *
     * @param action what the player asked for this turn
     * @throws NullPointerException if {@code action} is {@code null}
     */
    public void playTurn(TurnAction action) {
        Objects.requireNonNull(action, "action");
        turn++;
        engine.runTurn(this, action);
    }

    /** Returns the current state: in progress, won or lost. */
    public LevelState getState() {
        return state;
    }

    /** Returns the pond the level is played in. */
    public Pond getPond() {
        return pond;
    }

    /** Returns the throw bag. */
    public ThrowBag getBag() {
        return bag;
    }

    /** Returns the number of turns played so far. */
    public int getTurn() {
        return turn;
    }

    /** Returns the number of turns allowed. */
    public int getTurnLimit() {
        return turnLimit;
    }

    /**
     * Registers a listener to receive every event this level publishes.
     *
     * @param listener the listener to add
     * @throws NullPointerException if {@code listener} is {@code null}
     */
    public void addListener(PondListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    /**
     * Sends an event to every registered listener, in the order they were added.
     *
     * @param event the event to send
     * @throws NullPointerException if {@code event} is {@code null}
     */
    public void publish(GameEvent event) {
        Objects.requireNonNull(event, "event");
        for (PondListener listener : listeners) {
            listener.onEvent(event);
        }
    }
}