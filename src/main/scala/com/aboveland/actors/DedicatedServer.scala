package com.aboveland.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import com.aboveland.actors.DedicatedServerManager.{RegisterServer, RegisterServerResponse}
import com.aboveland.models.BaseServer

object DedicatedServer {

  sealed trait Command
  case class UpdateServer(server: BaseServer, replyTo: ActorRef[RegisterServerResponse]) extends Command

  def apply(server: BaseServer): Behavior[Command] = Behaviors.setup { context =>
    context.log.info("DedicatedServer started")
    run(server)
  }

  private def run(server: BaseServer): Behavior[Command] = Behaviors.receive { (ctx, cmd) =>
    cmd match {
      case UpdateServer(server, replyTo) =>
        ctx.log.info("UpdateServer: {}", server)
        replyTo ! RegisterServerResponse(server)
        Behaviors.same
    }
  }
}
