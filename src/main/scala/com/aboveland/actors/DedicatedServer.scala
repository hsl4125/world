package com.aboveland.actors

import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.{Behaviors, Routers}
import com.aboveland.models.{BaseServer, BaseServerStatus, BaseServerType}

import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
 * DedicatedServer Actor - Manages individual server state and lifecycle
 */
object DedicatedServer {

  // Command messages
  sealed trait Command
  case class StartServer(server: BaseServer) extends Command
  case class StopServer(reason: String) extends Command
  case class UpdateStatus(status: Int) extends Command
  case class GetStatus(replyTo: ActorRef[ServerStatusResponse]) extends Command
  case class UpdateServerInfo(server: BaseServer) extends Command
  case class HealthCheck(replyTo: ActorRef[HealthCheckResponse]) extends Command
  case class ServerStarted(server: BaseServer) extends Command
  case class ServerStopped(reason: String) extends Command
  case class ServerError(error: String) extends Command
  
  // Response messages
  sealed trait Response
  case class ServerStatusResponse(status: Int, server: BaseServer) extends Response
  case class HealthCheckResponse(isHealthy: Boolean, server: BaseServer) extends Response
  
  // Internal messages
  private case object CheckServerHealth extends Command
  private case object Heartbeat extends Command

  def apply(): Behavior[Command] = Behaviors.setup { context =>
    context.log.info("DedicatedServer actor started")
    
    Behaviors.withTimers { timers =>
      // Start periodic health check
      timers.startTimerWithFixedDelay(CheckServerHealth, 30.seconds)
      timers.startTimerWithFixedDelay(Heartbeat, 10.seconds)
      
      idle(context)
    }
  }

  private def idle(context: akka.actor.typed.scaladsl.ActorContext[Command]): Behavior[Command] = 
    Behaviors.receiveMessage {
      case StartServer(server) =>
        context.log.info("Starting server: {} (ID: {})", server.machineId, server.sid)
        starting(context, server)
        
      case GetStatus(replyTo) =>
        replyTo ! ServerStatusResponse(BaseServerStatus.STOPPED, BaseServerExtensions.empty)
        Behaviors.same
        
      case HealthCheck(replyTo) =>
        replyTo ! HealthCheckResponse(false, BaseServerExtensions.empty)
        Behaviors.same
        
      case CheckServerHealth =>
        // No server running, stay idle
        Behaviors.same
        
      case Heartbeat =>
        // No server running, stay idle
        Behaviors.same
        
      case _ =>
        context.log.warn("Received unexpected command in idle state")
        Behaviors.same
    }

  private def starting(context: akka.actor.typed.scaladsl.ActorContext[Command], server: BaseServer): Behavior[Command] = 
    Behaviors.receiveMessage {
      case ServerStarted(updatedServer) =>
        context.log.info("Server started successfully: {} at {}:{}", 
          updatedServer.machineId, updatedServer.ip, updatedServer.port)
        running(context, updatedServer)
        
      case ServerError(error) =>
        context.log.error("Failed to start server {}: {}", server.machineId, error)
        idle(context)
        
      case StopServer(reason) =>
        context.log.info("Stopping server {} during startup: {}", server.machineId, reason)
        idle(context)
        
      case GetStatus(replyTo) =>
        replyTo ! ServerStatusResponse(BaseServerStatus.STARTING, server)
        Behaviors.same
        
      case HealthCheck(replyTo) =>
        replyTo ! HealthCheckResponse(false, server)
        Behaviors.same
        
      case CheckServerHealth =>
        // Server is starting, check if it's taking too long
        context.log.debug("Server {} is still starting...", server.machineId)
        Behaviors.same
        
      case _ =>
        context.log.warn("Received unexpected command in starting state")
        Behaviors.same
    }

  private def running(context: akka.actor.typed.scaladsl.ActorContext[Command], server: BaseServer): Behavior[Command] = 
    Behaviors.receiveMessage {
      case StopServer(reason) =>
        context.log.info("Stopping server {}: {}", server.machineId, reason)
        stopping(context, server, reason)
        
      case UpdateStatus(newStatus) =>
        val updatedServer = server.copy(status = newStatus)
        context.log.info("Server {} status updated to: {}", server.machineId, newStatus)
        running(context, updatedServer)
        
      case UpdateServerInfo(updatedServer) =>
        context.log.info("Server info updated for {}", updatedServer.machineId)
        running(context, updatedServer)
        
      case GetStatus(replyTo) =>
        replyTo ! ServerStatusResponse(server.status, server)
        Behaviors.same
        
      case HealthCheck(replyTo) =>
        val isHealthy = server.status == BaseServerStatus.RUNNING
        replyTo ! HealthCheckResponse(isHealthy, server)
        Behaviors.same
        
      case CheckServerHealth =>
        // Perform actual health check logic here
        context.log.debug("Performing health check for server {}", server.machineId)
        Behaviors.same
        
      case Heartbeat =>
        context.log.debug("Server {} heartbeat", server.machineId)
        Behaviors.same
        
      case _ =>
        context.log.warn("Received unexpected command in running state")
        Behaviors.same
    }

  private def stopping(context: akka.actor.typed.scaladsl.ActorContext[Command], server: BaseServer, reason: String): Behavior[Command] = 
    Behaviors.receiveMessage {
      case ServerStopped(_) =>
        context.log.info("Server {} stopped successfully", server.machineId)
        idle(context)
        
      case ServerError(error) =>
        context.log.error("Error while stopping server {}: {}", server.machineId, error)
        idle(context)
        
      case GetStatus(replyTo) =>
        replyTo ! ServerStatusResponse(BaseServerStatus.STOPPING, server)
        Behaviors.same
        
      case HealthCheck(replyTo) =>
        replyTo ! HealthCheckResponse(false, server)
        Behaviors.same
        
      case CheckServerHealth =>
        context.log.debug("Server {} is stopping...", server.machineId)
        Behaviors.same
        
      case _ =>
        context.log.warn("Received unexpected command in stopping state")
        Behaviors.same
    }
}

/**
 * Extension methods for BaseServer
 */
// Extension methods for BaseServer
object BaseServerExtensions {
  def empty: BaseServer = BaseServer(
    sid = 0L,
    k8sCid = 0L,
    index = 0,
    machineId = "",
    serverType = BaseServerType.INSTANCE,
    status = BaseServerStatus.STOPPED,
    mapName = "",
    ip = "",
    port = 0,
    platform = "",
    zoneTag = "",
    debug = "",
    department = "",
    dungeonPlayerMax = 0,
    dungeonToken = ""
  )
}
