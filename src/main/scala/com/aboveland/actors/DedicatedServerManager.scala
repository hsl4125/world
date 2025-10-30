package com.aboveland.actors

import akka.actor.typed.{ActorRef, Behavior, Terminated}
import akka.actor.typed.scaladsl.Behaviors
import com.aboveland.actors.DedicatedServer.UpdateServer
import com.aboveland.models.{BaseServer, BaseServerStatus, ErrorCode, ErrorResponse, PlayerNumber}

object DedicatedServerManager {

  sealed trait Command
  case class RegisterServer(server: BaseServer, replyTo: ActorRef[RegisterServerResponse]) extends Command
  case class UpdatePlayerNumber(number: PlayerNumber, replyTo: ActorRef[UpdatePlayerNumberResponse]) extends Command


  sealed trait Response
  case class RegisterServerResponse(server: BaseServer) extends Response
  case class UpdatePlayerNumberResponse(response: Either[ErrorResponse, PlayerNumber]) extends Response

  // State to hold maxServerID and sidMap
  private case class ManagerState(
    maxServerID: Long,
    sidMap: collection.mutable.Map[Long, ActorRef[DedicatedServer.Command]]
  )

  def apply(): Behavior[Command] = Behaviors.setup { context =>
    context.log.info("DedicatedServerManager started")
    val initialState = ManagerState(0L, collection.mutable.Map.empty[Long, ActorRef[DedicatedServer.Command]])
    run(initialState)
  }

  private def run(state: ManagerState): Behavior[Command] = 
    Behaviors.receive[Command] { (ctx, cmd) =>
      cmd match {
        case RegisterServer(server, replyTo) =>
          ctx.child(makeServerName(server)) match {
            case Some(serverRef) =>
              ctx.log.info("RegisterServer need update ds server: {}", server)
              serverRef.asInstanceOf[ActorRef[DedicatedServer.Command]] ! UpdateServer(server, replyTo)
              run(state)
            case None =>
              // Increment maxServerID and assign it to the new server
              val newServerID = state.maxServerID + 1
              val updatedServer = server.copy(sid = newServerID, status = BaseServerStatus.STARTING)

              ctx.log.info("RegisterServer: {} with assigned ID: {}", server.machineId, newServerID)

              // Create DedicatedServer actor with the updated server (new sid)
              val serverName = makeServerName(updatedServer)
              val dsRef = ctx.spawn(DedicatedServer(updatedServer), serverName)

              // Watch the DedicatedServer actor to monitor its lifecycle
              ctx.watch(dsRef)

              // Reply with the updated server
              replyTo ! RegisterServerResponse(updatedServer)

              // Add to sidMap
              state.sidMap += (newServerID -> dsRef)

              // Continue with updated state
              run(state.copy(maxServerID = newServerID))
          }
        case UpdatePlayerNumber(number, replyTo) =>
          ctx.log.info("UpdatePlayerNumber: {}", number)
          state.sidMap.get(number.sid) match {
            case Some(serverRef) =>
              serverRef ! DedicatedServer.UpdatePlayerNumber(number, replyTo)
              Behaviors.same
            case None =>
              ctx.log.error("UpdatePlayerNumber: sid[{}] not found", number.sid)
              replyTo ! UpdatePlayerNumberResponse(Left(ErrorResponse(ErrorCode.ERROR_NUMBERS, s"UpdatePlayerNumber: ${number.sid} not found")))
              Behaviors.same
          }
      }
    }.receiveSignal {
      case (ctx, Terminated(ref)) =>
        // Find the sid associated with this terminated actor and remove it from sidMap
        val sidToRemove = state.sidMap.find { case (_, actorRef) => actorRef == ref }
        sidToRemove match {
          case Some((sid, _)) =>
            state.sidMap -= sid
            ctx.log.info("DedicatedServer with sid {} terminated and removed from sidMap", sid)
          case None =>
            ctx.log.warn("Received Terminated signal for unknown actor: {}", ref.path)
        }
        run(state)
    }

  private def makeServerName(server: BaseServer): String = {
    s"ds-${server.machineId}-${server.port}"
  }
}
