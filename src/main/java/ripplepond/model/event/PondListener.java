package ripplepond.model.event;

/** Receives events published by the pond. */
public interface PondListener {

    /** Called once for each event the pond publishes. */
    void onEvent(GameEvent event);
}
