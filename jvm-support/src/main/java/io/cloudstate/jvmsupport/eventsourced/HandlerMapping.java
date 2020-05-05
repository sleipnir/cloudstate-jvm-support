package io.cloudstate.jvmsupport.eventsourced;

import io.cloudstate.jvmsupport.impl.ReflectionHelper;
import io.cloudstate.jvmsupport.impl.eventsourced.EventHandlerInvoker;
import io.cloudstate.jvmsupport.impl.eventsourced.SnapshotHandlerInvoker;
import io.cloudstate.jvmsupport.impl.eventsourced.SnapshotInvoker;

import java.util.Optional;

public interface HandlerMapping {
  Optional<SnapshotInvoker> getSnapshotInvoker();

  Optional<EventHandlerInvoker> getEventHandlerForClass(Class<?> clazz);

  Optional<SnapshotHandlerInvoker> getSnapshotHandlerForClass(Class<?> clazz);

  Optional<ReflectionHelper.CommandHandlerInvoker> getCommandHandlerForMethod(String name);
}
