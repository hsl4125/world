package com.aboveland.example.services

import com.aboveland.example.models.{User, CreateUserRequest, UpdateUserRequest}
import com.aboveland.example.repository.UserRepository
import scala.concurrent.{Future, ExecutionContext}
import java.time.LocalDateTime
import java.util.UUID

class UserService(userRepository: UserRepository)(implicit ec: ExecutionContext) {
  
  def findAll(): Future[Seq[User]] = {
    userRepository.findAll()
  }
  
  def findById(id: String): Future[Option[User]] = {
    userRepository.findById(id)
  }
  
  def findByUsername(username: String): Future[Option[User]] = {
    userRepository.findByUsername(username)
  }
  
  def createUser(request: CreateUserRequest): Future[User] = {
    // Validate if username already exists
    userRepository.findByUsername(request.username).flatMap {
      case Some(_) =>
        Future.failed(new IllegalArgumentException(s"Username '${request.username}' already exists"))
      case None =>
        // Create new user
        val newUser = User(
          id = UUID.randomUUID().toString,
          username = request.username,
          email = request.email,
          firstName = request.firstName,
          lastName = request.lastName,
          createdAt = LocalDateTime.now(),
          updatedAt = LocalDateTime.now()
        )
        userRepository.save(newUser)
    }
  }
  
  def updateUser(id: String, request: UpdateUserRequest): Future[Option[User]] = {
    userRepository.findById(id).flatMap {
      case Some(existingUser) =>
        val updatedUser = existingUser.copy(
          email = request.email.getOrElse(existingUser.email),
          firstName = request.firstName.orElse(existingUser.firstName),
          lastName = request.lastName.orElse(existingUser.lastName),
          updatedAt = LocalDateTime.now()
        )
        userRepository.update(id, updatedUser)
      case None =>
        Future.successful(None)
    }
  }
  
  def deleteUser(id: String): Future[Boolean] = {
    userRepository.delete(id)
  }
}
