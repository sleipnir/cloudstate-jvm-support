package io.cloudstate.jvmsupport.eventsourced;

/** Context for an event. */
public interface EventContext extends EventSourcedContext {
  /**
   * The sequence number of the current event being processed.
   *
   * @return The sequence number.
   */
  long sequenceNumber();
}
