package io.cloudstate.jvmsupport.impl.crdt

import io.cloudstate.jvmsupport.crdt.Crdt
import io.cloudstate.protocol.crdt.{CrdtDelta, CrdtState}

private[crdt] trait InternalCrdt extends Crdt {
  def name: String
  def hasDelta: Boolean
  def delta: Option[CrdtDelta.Delta]
  def resetDelta(): Unit
  def state: CrdtState.State
  def applyDelta: PartialFunction[CrdtDelta.Delta, Unit]
  def applyState: PartialFunction[CrdtState.State, Unit]
}
