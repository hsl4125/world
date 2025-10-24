package com.aboveland.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object DedicatedServerActor {

  sealed trait Command

  def apply(): Behavior[Command] = Behaviors.setup { context =>
    context.log.info("DedicatedServerActor started")

    Behaviors.same
  }
}
