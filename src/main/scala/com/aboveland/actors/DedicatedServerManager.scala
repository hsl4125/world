package com.aboveland.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import com.aboveland.models.BaseServer

object DedicatedServerManager {

  sealed trait Command
  case class RegisterServer(server: BaseServer, replyTo: ActorRef[RegisterServerResponse]) extends Command
  
  sealed trait Response
  case class RegisterServerResponse(server: BaseServer) extends Response

  def apply(): Behavior[Command] = Behaviors.setup { context =>
    context.log.info("DedicatedServerManager started")
    
    Behaviors.receive { (ctx, cmd) =>
      cmd match {
        case RegisterServer(server, replyTo) =>
          ctx.log.info("RegisterServer: {}", server)
          ctx.spawn(DedicatedServer(server), s"dedicated-server-${server.sid}")
          replyTo ! RegisterServerResponse(server)
          Behaviors.same
      }
    }
  }
}
