package io.cloudstate.jvmsupport.crdt;

import io.cloudstate.jvmsupport.ClientActionContext;
import io.cloudstate.jvmsupport.EffectContext;

import java.util.function.Function;

/**
 * The context for a subscription, passed with every invocation of a {@link
 * StreamedCommandContext#onChange(Function)} callback.
 */
public interface SubscriptionContext extends CrdtContext, EffectContext, ClientActionContext {
  /** End this stream. */
  void endStream();
}
