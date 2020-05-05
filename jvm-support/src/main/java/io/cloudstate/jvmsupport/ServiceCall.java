package io.cloudstate.jvmsupport;

import com.google.protobuf.Any;

/** Represents a call to a service, performed either as a forward, or as an effect. */
public interface ServiceCall {

  /**
   * The reference to the call.
   *
   * @return The reference to the call.
   */
  ServiceCallRef<?> ref();

  /**
   * The message to pass to the call when the call is invoked.
   *
   * @return The message to pass to the call, serialized as an {@link Any}.
   */
  Any message();
}
