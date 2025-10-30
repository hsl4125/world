package com.aboveland.api.routes

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import com.aboveland.api.handlers.{HealthHandler, WorldHandler}

class Routes(
  healthHandler: HealthHandler,
  worldHandler: WorldHandler,
) {
  
  def allRoutes: Route = {
    pathPrefix("world") {
      concat(
        worldHandler.routes
      )
    } ~
    pathPrefix("api" / "v1") {
      concat(
        healthHandler.routes
      )
    } ~
    // Root path redirects to health check
    path("") {
      redirect("/api/v1/health", akka.http.scaladsl.model.StatusCodes.TemporaryRedirect)
    }
  }
}
