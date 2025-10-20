package com.aboveland.services

import akka.actor.typed.{ActorRef, ActorSystem}
import com.aboveland.actors.{DedicatedServer, DedicatedServerManager}
import com.aboveland.models.{BaseServer, BaseServerStatus, BaseServerType}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
 * DedicatedServerService - Service layer for managing dedicated servers
 */
class DedicatedServerService(
  private val managerRef: ActorRef[DedicatedServerManager.Command]
)(implicit system: ActorSystem[_], ec: ExecutionContext) {

  /**
   * Create a new dedicated server
   */
  def createServer(server: BaseServer): Future[Boolean] = {
    val promise = scala.concurrent.Promise[Boolean]
    
    managerRef ! DedicatedServerManager.CreateServer(server)
    // For simplicity, we assume creation always succeeds
    // In a real implementation, you'd want to get confirmation
    Future.successful(true)
  }

  /**
   * Start a server by ID
   */
  def startServer(sid: Long): Future[Boolean] = {
    val promise = scala.concurrent.Promise[Boolean]
    
    managerRef ! DedicatedServerManager.StartServer(sid)
    // For simplicity, we assume start always succeeds
    Future.successful(true)
  }

  /**
   * Stop a server by ID
   */
  def stopServer(sid: Long, reason: String = "Manual stop"): Future[Boolean] = {
    val promise = scala.concurrent.Promise[Boolean]
    
    managerRef ! DedicatedServerManager.StopServer(sid, reason)
    // For simplicity, we assume stop always succeeds
    Future.successful(true)
  }

  /**
   * Remove a server by ID
   */
  def removeServer(sid: Long): Future[Boolean] = {
    val promise = scala.concurrent.Promise[Boolean]
    
    managerRef ! DedicatedServerManager.RemoveServer(sid)
    // For simplicity, we assume removal always succeeds
    Future.successful(true)
  }

  /**
   * Get server information by ID
   */
  def getServer(sid: Long): Future[Option[BaseServer]] = {
    val promise = scala.concurrent.Promise[Option[BaseServer]]
    
    managerRef ! DedicatedServerManager.GetServer(sid, system.ignoreRef)
    // For simplicity, return None
    // In a real implementation, you'd want to get the actual response
    Future.successful(None)
  }

  /**
   * Get all servers
   */
  def getAllServers(): Future[List[BaseServer]] = {
    val promise = scala.concurrent.Promise[List[BaseServer]]
    
    managerRef ! DedicatedServerManager.GetAllServers(system.ignoreRef)
    // For simplicity, return empty list
    // In a real implementation, you'd want to get the actual response
    Future.successful(List.empty)
  }

  /**
   * Get servers by type
   */
  def getServersByType(serverType: Int): Future[List[BaseServer]] = {
    val promise = scala.concurrent.Promise[List[BaseServer]]
    
    managerRef ! DedicatedServerManager.GetServersByType(serverType, system.ignoreRef)
    // For simplicity, return empty list
    // In a real implementation, you'd want to get the actual response
    Future.successful(List.empty)
  }

  /**
   * Get servers by status
   */
  def getServersByStatus(status: Int): Future[List[BaseServer]] = {
    val promise = scala.concurrent.Promise[List[BaseServer]]
    
    managerRef ! DedicatedServerManager.GetServersByStatus(status, system.ignoreRef)
    // For simplicity, return empty list
    // In a real implementation, you'd want to get the actual response
    Future.successful(List.empty)
  }

  /**
   * Update server information
   */
  def updateServer(sid: Long, server: BaseServer): Future[Boolean] = {
    val promise = scala.concurrent.Promise[Boolean]
    
    managerRef ! DedicatedServerManager.UpdateServer(sid, server)
    // For simplicity, we assume update always succeeds
    Future.successful(true)
  }

  /**
   * Get manager health status
   */
  def getManagerHealth(): Future[String] = {
    val promise = scala.concurrent.Promise[String]
    
    managerRef ! DedicatedServerManager.HealthCheck(system.ignoreRef)
    // For simplicity, return healthy status
    // In a real implementation, you'd want to get the actual response
    Future.successful("Manager is healthy")
  }

  /**
   * Get manager statistics
   */
  def getManagerStats(): Future[String] = {
    val promise = scala.concurrent.Promise[String]
    
    managerRef ! DedicatedServerManager.GetManagerStats(system.ignoreRef)
    // For simplicity, return basic stats
    // In a real implementation, you'd want to get the actual response
    Future.successful("Total servers: 0, Running: 0")
  }
}
