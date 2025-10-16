package com.aboveland.api.handlers

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import com.aboveland.api.services.{HealthService, HealthStatus}
import spray.json._
import com.aboveland.api.utils.JsonSupport._

object HealthStatusJsonProtocol extends DefaultJsonProtocol {
  implicit val healthStatusFormat: JsonFormat[HealthStatus] = jsonFormat4(HealthStatus.apply)
}

class HealthHandler(healthService: HealthService) {
  
  import HealthStatusJsonProtocol._
  
  def routes: Route = {
    pathPrefix("health") {
      concat(
        // GET /api/v1/health - Health check
        get {
          onSuccess(healthService.getHealthStatus()) { healthStatus =>
            complete(StatusCodes.OK, healthStatus.toJson.compactPrint)
          }
        },
        
        // GET /api/v1/health/ready - Readiness check
        path("ready") {
          get {
            complete(StatusCodes.OK, """{"status": "ready", "message": "Service is ready"}""")
          }
        },
        
        // GET /api/v1/health/live - Liveness check
        path("live") {
          get {
            complete(StatusCodes.OK, """{"status": "alive", "message": "Service is alive"}""")
          }
        }
      )
    }
  }
}