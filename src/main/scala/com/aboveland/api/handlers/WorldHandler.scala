package com.aboveland.api.handlers

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import com.aboveland.api.services.WorldService
import com.aboveland.api.utils.JsonSupport._
import com.aboveland.models.{BaseServer, PlayerNumber}

class WorldHandler(worldService: WorldService) {
  
  def routes: Route = {
    concat(
      // POST /world/get
      path("get") {
        post {
          onSuccess(worldService.getWorld()) { result =>
            complete(StatusCodes.OK, result)
          }
        }
      },

      // POST /world/register
      path("register") {
        post {
          entity(as[BaseServer]) { server =>
            onSuccess(worldService.registerWorld(server)) { result =>
              complete(StatusCodes.OK, result)
            }
          }
        }
      },
      
      // POST /world/number
      path("number") {
        post {
          entity(as[PlayerNumber]) { number =>
            onSuccess(worldService.updatePlayerNumber(number)) { result =>
              complete(StatusCodes.OK, result.response)
            }
          }
        }
      },
      
      // POST /world/portal
      path("portal") {
        post {
          onSuccess(worldService.getWorldPortal()) { result =>
            complete(StatusCodes.OK, result)
          }
        }
      },
      
      // POST /world/getDungeonMax
      path("getDungeonMax") {
        post {
          onSuccess(worldService.getDungeonMax()) { result =>
            complete(StatusCodes.OK, result)
          }
        }
      },
      
      // POST /world/checkDSState
      path("checkDSState") {
        post {
          onSuccess(worldService.checkDSState()) { result =>
            complete(StatusCodes.OK, result)
          }
        }
      }
    )
  }
}
