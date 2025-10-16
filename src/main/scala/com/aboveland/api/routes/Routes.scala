package com.aboveland.api.routes

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import com.aboveland.api.handlers.{HealthHandler, UserHandler}

class Routes(
  userHandler: UserHandler,
  healthHandler: HealthHandler
) {
  
  def allRoutes: Route = {
    pathPrefix("api" / "v1") {
      concat(
        healthHandler.routes,
        userHandler.routes
      )
    } ~
    // Root path redirects to health check
    path("") {
      redirect("/api/v1/health", akka.http.scaladsl.model.StatusCodes.TemporaryRedirect)
    } ~
    // Handle unmatched paths
    path(Remaining) { remaining =>
      complete(akka.http.scaladsl.model.StatusCodes.NotFound, 
        s"API endpoint not found: /$remaining")
    }
  }
}
