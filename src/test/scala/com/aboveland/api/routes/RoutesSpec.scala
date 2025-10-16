package com.aboveland.api.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.aboveland.api.handlers.{HealthHandler, UserHandler}
import com.aboveland.api.services.{HealthService, UserService}
import com.aboveland.api.repository.UserRepository
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class RoutesSpec extends AnyWordSpec with Matchers with ScalatestRouteTest {
  
  private val userRepository = new UserRepository()
  private val userService = new UserService(userRepository)
  private val healthService = new HealthService()
  
  private val userHandler = new UserHandler(userService)
  private val healthHandler = new HealthHandler(healthService)
  
  private val routes = new Routes(userHandler, healthHandler)
  
  "Routes" should {
    
    "return health status for GET /api/v1/health" in {
      Get("/api/v1/health") ~> routes.allRoutes ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] should include("success")
      }
    }
    
    "return ready status for GET /api/v1/health/ready" in {
      Get("/api/v1/health/ready") ~> routes.allRoutes ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] should include("ready")
      }
    }
    
    "return live status for GET /api/v1/health/live" in {
      Get("/api/v1/health/live") ~> routes.allRoutes ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] should include("alive")
      }
    }
    
    "return users list for GET /api/v1/users" in {
      Get("/api/v1/users") ~> routes.allRoutes ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] should include("success")
      }
    }
    
    "return 404 for unknown endpoints" in {
      Get("/api/v1/unknown") ~> routes.allRoutes ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }
  }
}
