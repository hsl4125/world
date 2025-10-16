package com.aboveland.example.handlers

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import com.aboveland.example.services.UserService
import com.aboveland.example.models.{User, CreateUserRequest, UpdateUserRequest}
import com.aboveland.example.models.UserModels._
import spray.json._
import com.aboveland.api.utils.JsonSupport._

class UserHandler(userService: UserService) {
  
  def routes: Route = {
    pathPrefix("users") {
      concat(
        // GET /api/v1/users - Get all users
        get {
          onSuccess(userService.findAll()) { users =>
            complete(StatusCodes.OK, users.toJson.compactPrint)
          }
        },
        
        // POST /api/v1/users - Create user
        post {
          entity(as[CreateUserRequest]) { request =>
            onSuccess(userService.createUser(request)) { user =>
              complete(StatusCodes.Created, user.toJson.compactPrint)
            }
          }
        },
        
        // GET /api/v1/users/{id} - Get specific user
        path(Segment) { userId =>
          concat(
            get {
              onSuccess(userService.findById(userId)) {
                case Some(user) =>
                  complete(StatusCodes.OK, user.toJson.compactPrint)
                case None =>
                  complete(StatusCodes.NotFound, """{"error": "User not found"}""")
              }
            },
            
            // PUT /api/v1/users/{id} - Update user
            put {
              entity(as[UpdateUserRequest]) { request =>
                onSuccess(userService.updateUser(userId, request)) {
                  case Some(user) =>
                    complete(StatusCodes.OK, user.toJson.compactPrint)
                  case None =>
                    complete(StatusCodes.NotFound, """{"error": "User not found"}""")
                }
              }
            },
            
            // DELETE /api/v1/users/{id} - Delete user
            delete {
              onSuccess(userService.deleteUser(userId)) { deleted =>
                if (deleted) {
                  complete(StatusCodes.OK, """{"message": "User deleted successfully"}""")
                } else {
                  complete(StatusCodes.NotFound, """{"error": "User not found"}""")
                }
              }
            }
          )
        }
      )
    }
  }
}
