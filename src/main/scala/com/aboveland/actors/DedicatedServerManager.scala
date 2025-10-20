package com.aboveland.actors

import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.{Behaviors, Routers}
import com.aboveland.models.{BaseServer, BaseServerStatus, BaseServerType}

import scala.collection.mutable
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
 * DedicatedServerManager Actor - Manages multiple DedicatedServer actors
 * Responsible for lifecycle management and supervision of DedicatedServer instances
 */
object DedicatedServerManager {

  // Command messages
  sealed trait Command
  case class CreateServer(server: BaseServer) extends Command
  case class StartServer(sid: Long) extends Command
  case class StopServer(sid: Long, reason: String) extends Command
  case class RemoveServer(sid: Long) extends Command
  case class GetServer(sid: Long, replyTo: ActorRef[ServerResponse]) extends Command
  case class GetAllServers(replyTo: ActorRef[AllServersResponse]) extends Command
  case class GetServersByType(serverType: Int, replyTo: ActorRef[AllServersResponse]) extends Command
  case class GetServersByStatus(status: Int, replyTo: ActorRef[AllServersResponse]) extends Command
  case class UpdateServer(sid: Long, server: BaseServer) extends Command
  case class HealthCheck(replyTo: ActorRef[ManagerHealthResponse]) extends Command
  case class GetManagerStats(replyTo: ActorRef[ManagerStatsResponse]) extends Command
  
  // Response messages
  sealed trait Response
  case class ServerResponse(server: Option[BaseServer], serverRef: Option[ActorRef[DedicatedServer.Command]]) extends Response
  case class AllServersResponse(servers: List[BaseServer]) extends Response
  case class ManagerHealthResponse(isHealthy: Boolean, activeServers: Int, totalServers: Int) extends Response
  case class ManagerStatsResponse(
    totalServers: Int,
    runningServers: Int,
    startingServers: Int,
    stoppingServers: Int,
    stoppedServers: Int,
    errorServers: Int
  ) extends Response
  
  // Internal messages
  private case class ServerCreated(sid: Long, serverRef: ActorRef[DedicatedServer.Command]) extends Command
  private case class ServerRemoved(sid: Long) extends Command
  private case class ServerStatusChanged(sid: Long, status: Int) extends Command
  private case object PeriodicHealthCheck extends Command
  private case object CleanupInactiveServers extends Command

  def apply(): Behavior[Command] = Behaviors.setup { context =>
    context.log.info("DedicatedServerManager started")
    
    Behaviors.withTimers { timers =>
      // Start periodic health check
      timers.startTimerWithFixedDelay(PeriodicHealthCheck, 30.seconds)
      timers.startTimerWithFixedDelay(CleanupInactiveServers, 60.seconds)
      
      active(context, mutable.Map.empty[Long, (BaseServer, ActorRef[DedicatedServer.Command])])
    }
  }

  private def active(
    context: akka.actor.typed.scaladsl.ActorContext[Command],
    servers: mutable.Map[Long, (BaseServer, ActorRef[DedicatedServer.Command])]
  ): Behavior[Command] = Behaviors.receiveMessage {
    
    case CreateServer(server) =>
      if (servers.contains(server.sid)) {
        context.log.warn("Server with ID {} already exists", server.sid)
        Behaviors.same
      } else {
        context.log.info("Creating new server: {} (ID: {})", server.machineId, server.sid)
        
        val serverRef = context.spawn(
          Behaviors.supervise(DedicatedServer())
            .onFailure[Exception](SupervisorStrategy.restart),
          s"dedicated-server-${server.sid}"
        )
        
        servers(server.sid) = (server, serverRef)
        context.log.info("Server {} created successfully", server.machineId)
        Behaviors.same
      }
      
    case StartServer(sid) =>
      servers.get(sid) match {
        case Some((server, serverRef)) =>
          context.log.info("Starting server: {} (ID: {})", server.machineId, sid)
          serverRef ! DedicatedServer.StartServer(server)
          Behaviors.same
        case None =>
          context.log.warn("Server with ID {} not found", sid)
          Behaviors.same
      }
      
    case StopServer(sid, reason) =>
      servers.get(sid) match {
        case Some((server, serverRef)) =>
          context.log.info("Stopping server: {} (ID: {}) - Reason: {}", server.machineId, sid, reason)
          serverRef ! DedicatedServer.StopServer(reason)
          Behaviors.same
        case None =>
          context.log.warn("Server with ID {} not found", sid)
          Behaviors.same
      }
      
    case RemoveServer(sid) =>
      servers.get(sid) match {
        case Some((server, serverRef)) =>
          context.log.info("Removing server: {} (ID: {})", server.machineId, sid)
          context.stop(serverRef)
          servers.remove(sid)
          Behaviors.same
        case None =>
          context.log.warn("Server with ID {} not found", sid)
          Behaviors.same
      }
      
    case GetServer(sid, replyTo) =>
      servers.get(sid) match {
        case Some((server, serverRef)) =>
          replyTo ! ServerResponse(Some(server), Some(serverRef))
        case None =>
          replyTo ! ServerResponse(None, None)
      }
      Behaviors.same
      
    case GetAllServers(replyTo) =>
      val serverList = servers.values.map(_._1).toList
      replyTo ! AllServersResponse(serverList)
      Behaviors.same
      
    case GetServersByType(serverType, replyTo) =>
      val serverList = servers.values
        .map(_._1)
        .filter(_.serverType == serverType)
        .toList
      replyTo ! AllServersResponse(serverList)
      Behaviors.same
      
    case GetServersByStatus(status, replyTo) =>
      val serverList = servers.values
        .map(_._1)
        .filter(_.status == status)
        .toList
      replyTo ! AllServersResponse(serverList)
      Behaviors.same
      
    case UpdateServer(sid, updatedServer) =>
      servers.get(sid) match {
        case Some((_, serverRef)) =>
          context.log.info("Updating server: {} (ID: {})", updatedServer.machineId, sid)
          servers(sid) = (updatedServer, serverRef)
          serverRef ! DedicatedServer.UpdateServerInfo(updatedServer)
          Behaviors.same
        case None =>
          context.log.warn("Server with ID {} not found for update", sid)
          Behaviors.same
      }
      
    case HealthCheck(replyTo) =>
      val activeServers = servers.count(_._2._1.status == BaseServerStatus.RUNNING)
      val totalServers = servers.size
      val isHealthy = totalServers > 0 && activeServers > 0
      replyTo ! ManagerHealthResponse(isHealthy, activeServers, totalServers)
      Behaviors.same
      
    case GetManagerStats(replyTo) =>
      val stats = calculateStats(servers)
      replyTo ! stats
      Behaviors.same
      
    case PeriodicHealthCheck =>
      context.log.debug("Performing periodic health check for {} servers", servers.size)
      servers.values.foreach { case (server, serverRef) =>
        // Perform health check - in real implementation, you'd want to collect responses
        context.log.debug("Health check for server {}", server.machineId)
      }
      Behaviors.same
      
    case CleanupInactiveServers =>
      val inactiveServers = servers.filter { case (_, (server, _)) =>
        server.status == BaseServerStatus.STOPPED || server.status == BaseServerStatus.ERROR
      }
      
      if (inactiveServers.nonEmpty) {
        context.log.info("Cleaning up {} inactive servers", inactiveServers.size)
        inactiveServers.foreach { case (sid, (_, serverRef)) =>
          context.stop(serverRef)
          servers.remove(sid)
        }
      }
      Behaviors.same
      
    case _ =>
      context.log.warn("Received unexpected command")
      Behaviors.same
  }

  private def calculateStats(servers: mutable.Map[Long, (BaseServer, ActorRef[DedicatedServer.Command])]): ManagerStatsResponse = {
    val serverList = servers.values.map(_._1).toList
    
    ManagerStatsResponse(
      totalServers = serverList.size,
      runningServers = serverList.count(_.status == BaseServerStatus.RUNNING),
      startingServers = serverList.count(_.status == BaseServerStatus.STARTING),
      stoppingServers = serverList.count(_.status == BaseServerStatus.STOPPING),
      stoppedServers = serverList.count(_.status == BaseServerStatus.STOPPED),
      errorServers = serverList.count(_.status == BaseServerStatus.ERROR)
    )
  }
}
