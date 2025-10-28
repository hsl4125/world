package com.aboveland.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import com.aboveland.actors.DedicatedServer.UpdateServer
import com.aboveland.models.{BaseServer, BaseServerStatus}

object DedicatedServerManager {

  sealed trait Command
  case class RegisterServer(server: BaseServer, replyTo: ActorRef[RegisterServerResponse]) extends Command
  
  sealed trait Response
  case class RegisterServerResponse(server: BaseServer) extends Response

  def apply(): Behavior[Command] = Behaviors.setup { context =>
    context.log.info("DedicatedServerManager started")
    run(0L) // Start with maxServerID = 0
  }

  private def run(maxServerID: Long): Behavior[Command] = Behaviors.receive { (ctx, cmd) =>
    cmd match {
      case RegisterServer(server, replyTo) =>
        ctx.child(makeServerName(server)) match {
          case Some(serverRef) =>
            ctx.log.info("RegisterServer need update: {}", server)
            serverRef.asInstanceOf[ActorRef[DedicatedServer.Command]] ! UpdateServer(server, replyTo)
            // TODO: [error] match may not be exhaustive. case: Some(_)
            // serverRef ! UpdateServer(server, replyTo)
            run(maxServerID)
          case None =>
            // Increment maxServerID and assign it to the new server
            val newServerID = maxServerID + 1
            val updatedServer = server.copy(sid = newServerID, status = BaseServerStatus.STARTING)

            ctx.log.info("RegisterServer: {} with assigned ID: {}", server.machineId, newServerID)

            // Create DedicatedServer actor with the updated server (new sid)
            val serverName = makeServerName(updatedServer)
            ctx.spawn(DedicatedServer(updatedServer), serverName)

            // Reply with the updated server
            replyTo ! RegisterServerResponse(updatedServer)

            // Continue with updated maxServerID state
            run(newServerID)
        }
    }
  }

  private def makeServerName(server: BaseServer): String = {
    s"ds-${server.machineId}-${server.port}"
  }
}
