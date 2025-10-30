package com.aboveland.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import com.aboveland.actors.DedicatedServerManager.{RegisterServerResponse, UpdatePlayerNumberResponse}
import com.aboveland.models.{BaseServer, PlayerNumber, ServerState}

import java.time.Instant

object DedicatedServer {

  sealed trait Command
  case class UpdateServer(server: BaseServer, replyTo: ActorRef[RegisterServerResponse]) extends Command
  case class UpdatePlayerNumber(number: PlayerNumber, replyTo: ActorRef[UpdatePlayerNumberResponse]) extends Command

  def apply(server: BaseServer): Behavior[Command] = Behaviors.setup { context =>
    context.log.info("DedicatedServer[{}] started", context.self.path)

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
        ctx.log.info("DedicatedServer[{}] update: {}", ctx.self.path, server)
        replyTo ! RegisterServerResponse(server)
        val newServer = serverState.copy(baseInfo = server)
        run(newServer)

      case UpdatePlayerNumber(number, replyTo) =>
        val newServer = serverState.copy(players = number.players)
        ctx.log.info("DedicatedServer[{}] update player number: {}", ctx.self.path, number)
        replyTo ! UpdatePlayerNumberResponse(Right(number))
        run(newServer)
    }
  }
}
