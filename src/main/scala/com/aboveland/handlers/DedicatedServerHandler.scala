package com.aboveland.handlers

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json._
import com.aboveland.models.{BaseServer, BaseServerStatus, BaseServerType}
import com.aboveland.services.DedicatedServerService

import scala.concurrent.ExecutionContext

/**
 * DedicatedServerHandler - HTTP handler for dedicated server operations
 */
class DedicatedServerHandler(
  private val dedicatedServerService: DedicatedServerService
)(implicit system: ActorSystem[_], ec: ExecutionContext) {

  // JSON protocols
  object BaseServerJsonProtocol extends DefaultJsonProtocol {
    implicit val baseServerFormat: RootJsonFormat[BaseServer] = jsonFormat15(BaseServer.apply)
    implicit val listBaseServerFormat: RootJsonFormat[List[BaseServer]] = listFormat(baseServerFormat)
  }
  
  import BaseServerJsonProtocol._

  def routes: Route = {
    concat(
        // POST /dedicated-server/create - Create a new server
        post {
          path("create") {
            entity(as[BaseServer]) { server =>
              onSuccess(dedicatedServerService.createServer(server)) { success =>
                if (success) {
                  complete(StatusCodes.OK, s"Server ${server.machineId} created successfully")
                } else {
                  complete(StatusCodes.InternalServerError, "Failed to create server")
                }
              }
            }
          }
        },
        
        // POST /dedicated-server/start - Start a server
        post {
          path("start") {
            parameters("sid".as[Long]) { sid =>
              onSuccess(dedicatedServerService.startServer(sid)) { success =>
                if (success) {
                  complete(StatusCodes.OK, s"Server $sid started successfully")
                } else {
                  complete(StatusCodes.InternalServerError, s"Failed to start server $sid")
                }
              }
            }
          }
        },
        
        // POST /dedicated-server/stop - Stop a server
        post {
          path("stop") {
            parameters("sid".as[Long], "reason".optional) { (sid, reason) =>
              onSuccess(dedicatedServerService.stopServer(sid, reason.getOrElse("Manual stop"))) { success =>
                if (success) {
                  complete(StatusCodes.OK, s"Server $sid stopped successfully")
                } else {
                  complete(StatusCodes.InternalServerError, s"Failed to stop server $sid")
                }
              }
            }
          }
        },
        
        // DELETE /dedicated-server/remove - Remove a server
        delete {
          path("remove") {
            parameters("sid".as[Long]) { sid =>
              onSuccess(dedicatedServerService.removeServer(sid)) { success =>
                if (success) {
                  complete(StatusCodes.OK, s"Server $sid removed successfully")
                } else {
                  complete(StatusCodes.InternalServerError, s"Failed to remove server $sid")
                }
              }
            }
          }
        },
        
        // GET /dedicated-server/get - Get server by ID
        get {
          path("get") {
            parameters("sid".as[Long]) { sid =>
              onSuccess(dedicatedServerService.getServer(sid)) { serverOption =>
                serverOption match {
                  case Some(server) =>
                    complete(StatusCodes.OK, server)
                  case None =>
                    complete(StatusCodes.NotFound, s"Server $sid not found")
                }
              }
            }
          }
        },
        
        // GET /dedicated-server/all - Get all servers
        get {
          path("all") {
            onSuccess(dedicatedServerService.getAllServers()) { servers =>
              complete(StatusCodes.OK, servers)
            }
          }
        },
        
        // GET /dedicated-server/by-type - Get servers by type
        get {
          path("by-type") {
            parameters("type".as[Int]) { serverType =>
              onSuccess(dedicatedServerService.getServersByType(serverType)) { servers =>
                complete(StatusCodes.OK, servers)
              }
            }
          }
        },
        
        // GET /dedicated-server/by-status - Get servers by status
        get {
          path("by-status") {
            parameters("status".as[Int]) { status =>
              onSuccess(dedicatedServerService.getServersByStatus(status)) { servers =>
                complete(StatusCodes.OK, servers)
              }
            }
          }
        },
        
        // PUT /dedicated-server/update - Update server information
        put {
          path("update") {
            entity(as[BaseServer]) { server =>
              onSuccess(dedicatedServerService.updateServer(server.sid, server)) { success =>
                if (success) {
                  complete(StatusCodes.OK, s"Server ${server.machineId} updated successfully")
                } else {
                  complete(StatusCodes.InternalServerError, "Failed to update server")
                }
              }
            }
          }
        },
        
        // GET /dedicated-server/health - Get manager health
        get {
          path("health") {
            onSuccess(dedicatedServerService.getManagerHealth()) { health =>
              complete(StatusCodes.OK, health)
            }
          }
        },
        
        // GET /dedicated-server/stats - Get manager statistics
        get {
          path("stats") {
            onSuccess(dedicatedServerService.getManagerStats()) { stats =>
              complete(StatusCodes.OK, stats)
            }
          }
        }
    )
  }
}
