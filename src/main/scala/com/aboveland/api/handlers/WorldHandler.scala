package com.aboveland.api.handlers

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import com.aboveland.api.services.WorldService

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
          onSuccess(worldService.registerWorld()) { result =>
            complete(StatusCodes.OK, result)
          }
        }
      },
      
      // POST /world/number
      path("number") {
        post {
          onSuccess(worldService.getWorldNumber()) { result =>
            complete(StatusCodes.OK, result)
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
