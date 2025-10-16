package com.aboveland.api.models

import java.time.LocalDateTime
import spray.json.DefaultJsonProtocol

// User data model
case class User(
  id: String,
  username: String,
  email: String,
  firstName: Option[String],
  lastName: Option[String],
  createdAt: LocalDateTime,
  updatedAt: LocalDateTime
)

// Create user request
case class CreateUserRequest(
  username: String,
  email: String,
  firstName: Option[String] = None,
  lastName: Option[String] = None
)

// Update user request
case class UpdateUserRequest(
  email: Option[String] = None,
  firstName: Option[String] = None,
  lastName: Option[String] = None
)

// JSON serialization support
object UserModels extends DefaultJsonProtocol {
  import spray.json._
  import com.aboveland.api.utils.JsonSupport._
  
  implicit val userFormat: JsonFormat[User] = jsonFormat7(User.apply)
  implicit val createUserRequestFormat: JsonFormat[CreateUserRequest] = jsonFormat4(CreateUserRequest.apply)
  implicit val updateUserRequestFormat: JsonFormat[UpdateUserRequest] = jsonFormat3(UpdateUserRequest.apply)
  
  // Provide RootJsonReader for Akka HTTP
  implicit val createUserRequestReader: RootJsonReader[CreateUserRequest] = createUserRequestFormat.asInstanceOf[RootJsonReader[CreateUserRequest]]
  implicit val updateUserRequestReader: RootJsonReader[UpdateUserRequest] = updateUserRequestFormat.asInstanceOf[RootJsonReader[UpdateUserRequest]]
}
