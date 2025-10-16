package com.aboveland.example.repository

import com.aboveland.example.models.User
import java.time.LocalDateTime
import scala.concurrent.Future
import scala.collection.mutable

// User data access interface
trait UserRepository {
  def findAll(): Future[Seq[User]]
  def findById(id: String): Future[Option[User]]
  def findByUsername(username: String): Future[Option[User]]
  def save(user: User): Future[User]
  def update(id: String, user: User): Future[Option[User]]
  def delete(id: String): Future[Boolean]
}

// In-memory implementation (for demo purposes, should use database in production)
class InMemoryUserRepository extends UserRepository {
  private val users = mutable.Map[String, User]()
  
  // Initialize some sample data
  users ++= Map(
    "1" -> User(
      id = "1",
      username = "admin",
      email = "admin@aboveland.com",
      firstName = Some("Admin"),
      lastName = Some("User"),
      createdAt = LocalDateTime.now().minusDays(30),
      updatedAt = LocalDateTime.now()
    ),
    "2" -> User(
      id = "2",
      username = "john_doe",
      email = "john@example.com",
      firstName = Some("John"),
      lastName = Some("Doe"),
      createdAt = LocalDateTime.now().minusDays(15),
      updatedAt = LocalDateTime.now()
    )
  )
  
  override def findAll(): Future[Seq[User]] = {
    Future.successful(users.values.toSeq)
  }
  
  override def findById(id: String): Future[Option[User]] = {
    Future.successful(users.get(id))
  }
  
  override def findByUsername(username: String): Future[Option[User]] = {
    Future.successful(users.values.find(_.username == username))
  }
  
  override def save(user: User): Future[User] = {
    val userWithId = user.copy(
      id = if (user.id.isEmpty) java.util.UUID.randomUUID().toString else user.id,
      createdAt = if (user.createdAt == null) LocalDateTime.now() else user.createdAt,
      updatedAt = LocalDateTime.now()
    )
    users += (userWithId.id -> userWithId)
    Future.successful(userWithId)
  }
  
  override def update(id: String, user: User): Future[Option[User]] = {
    users.get(id) match {
      case Some(existingUser) =>
        val updatedUser = user.copy(
          id = id,
          createdAt = existingUser.createdAt,
          updatedAt = LocalDateTime.now()
        )
        users += (id -> updatedUser)
        Future.successful(Some(updatedUser))
      case None =>
        Future.successful(None)
    }
  }
  
  override def delete(id: String): Future[Boolean] = {
    users.remove(id) match {
      case Some(_) => Future.successful(true)
      case None => Future.successful(false)
    }
  }
}
