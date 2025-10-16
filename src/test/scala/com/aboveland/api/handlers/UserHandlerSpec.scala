package com.aboveland.api.handlers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.aboveland.api.models.CreateUserRequest
import com.aboveland.api.services.UserService
import com.aboveland.api.repository.UserRepository
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import spray.json._

class UserHandlerSpec extends AnyWordSpec with Matchers with ScalatestRouteTest {
  
  private val userRepository = new UserRepository()
  private val userService = new UserService(userRepository)
  private val userHandler = new UserHandler(userService)
  
  "UserHandler" should {
    
    "return all users for GET /users" in {
      Get("/users") ~> userHandler.routes ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] should include("success")
      }
    }
    
    "create a new user for POST /users" in {
      val createRequest = CreateUserRequest(
        username = "testuser",
        email = "test@example.com",
        firstName = Some("Test"),
        lastName = Some("User")
      )
      
      val requestJson = createRequest.toJson.compactPrint
      
      Post("/users").withEntity(requestJson) ~> userHandler.routes ~> check {
        status shouldBe StatusCodes.Created
        responseAs[String] should include("created successfully")
      }
    }
    
    "return 404 for non-existent user" in {
      Get("/users/nonexistent") ~> userHandler.routes ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }
  }
}
