package com.aboveland.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import com.aboveland.actors.DedicatedServerManager.{RegisterServer, RegisterServerResponse}
import com.aboveland.models.{BaseServer, ServerState}

import java.time.Instant

object DedicatedServer {

  sealed trait Command
  case class UpdateServer(server: BaseServer, replyTo: ActorRef[RegisterServerResponse]) extends Command

  def apply(server: BaseServer): Behavior[Command] = Behaviors.setup { context =>
    context.log.info("DedicatedServer started")

    val serverState = ServerState(
      baseInfo = server,
      pendingStatus = 0,
      lastTime = Instant.now(),
      enterPendingTime = Instant.now(),
      players = 0,
      running = false,
      k8sDestroy = false
    )

    run(serverState)
  }

  private def run(serverState: ServerState): Behavior[Command] = Behaviors.receive { (ctx, cmd) =>
    cmd match {
      case UpdateServer(server, replyTo) =>
        ctx.log.info("UpdateServer: {}", server)
        replyTo ! RegisterServerResponse(server)
        Behaviors.same
    }
  }
}
