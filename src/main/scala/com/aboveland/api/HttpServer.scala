package com.aboveland.api

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import com.aboveland.api.config.AppConfig
import com.aboveland.api.middleware.{CorsDirectives, ErrorHandlingDirectives, LoggingDirectives}
import com.aboveland.api.routes.Routes
import com.aboveland.api.services.{HealthService, UserService}
import com.aboveland.api.repository.{UserRepository, InMemoryUserRepository}
import com.aboveland.api.handlers.{HealthHandler, UserHandler}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object HttpServer {
  
  def main(args: Array[String]): Unit = {
    // Create Actor system
    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "yyy-api-system")
    implicit val ec: ExecutionContext = system.executionContext
    
    // Load configuration
    val appConfig = AppConfig.load()
    
    // Create service layer
    val userRepository = new InMemoryUserRepository() // Use in-memory implementation
    val userService = new UserService(userRepository)
    val healthService = new HealthService()
    
    // Create handlers
    val userHandler = new UserHandler(userService)
    val healthHandler = new HealthHandler(healthService)
    
    // Create routes
    val routes = new Routes(userHandler, healthHandler)
    
    // Combine all routes and middleware
    val allRoutes: Route = {
      CorsDirectives.corsHandler {
        ErrorHandlingDirectives.handleErrors {
          LoggingDirectives.logRequests {
            routes.allRoutes
          }
        }
      }
    }
    
    // Start HTTP server
    val bindingFuture = Http().newServerAt(appConfig.server.host, appConfig.server.port)
      .bind(allRoutes)
    
    bindingFuture.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
        println(s"🚀 Server is running at http://${address.getHostString}:${address.getPort}")
        println("📝 Press ENTER to stop the server...")
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }(ec)
    
    // Graceful shutdown hook
    sys.addShutdownHook {
      system.log.info("Shutting down server...")
      bindingFuture
        .flatMap(_.unbind())(ec)
        .onComplete(_ => system.terminate())(ec)
    }
    
    // Block main thread to keep server running
    try {
      // Use CountDownLatch to keep server running until shutdown signal received
      val latch = new java.util.concurrent.CountDownLatch(1)
      
      // Add graceful shutdown hook
      sys.addShutdownHook {
        system.log.info("Received shutdown signal...")
        latch.countDown()
      }
      
      // Block waiting for shutdown signal
      latch.await()
    } catch {
      case _: InterruptedException => // Ignore interrupt exception
        system.log.info("Server interrupted")
    }
    
    // Manually shutdown server
    system.log.info("Shutting down server...")
    bindingFuture
      .flatMap(_.unbind())(ec)
      .onComplete(_ => system.terminate())(ec)
  }
}
