package ripplepond.model.level;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * Creates a level at turn 0.
     *
     * @param pond      the pond the level is played in
     * @param bag       the objects the player can throw
     * @param turnLimit the number of turns allowed
     * @param state     the state the level starts in
     */
    public Level(Pond pond, ThrowBag bag, int turnLimit, LevelState state) {
        this.pond = pond;
        this.bag = bag;
        this.turnLimit = turnLimit;
        this.state = state;
        this.turn = 0;
    }

    /**
     * Plays one turn. Implemented in S5.
     *
     * @param action what the player asked for this turn
     * @throws UnsupportedOperationException until S5 is merged
     */
    public void playTurn(TurnAction action) {
        throw new UnsupportedOperationException("Implemented in S5");
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
     */
    public void addListener(PondListener listener) {
        listeners.add(listener);
    }

    /**
     * Sends an event to every registered listener, in the order they were added.
     *
     * @param event the event to send
     */
    public void publish(GameEvent event) {
        for (PondListener listener : listeners) {
            listener.onEvent(event);
        }
    }
}