package io.cloudstate.jvmsupport;

/** Root class of all contexts. */
public interface Context {
  /** Get the service call factory for this stateful service. */
  ServiceCallFactory serviceCallFactory();
}
