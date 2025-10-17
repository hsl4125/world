package com.aboveland.api

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import com.aboveland.api.config.AppConfig
import com.aboveland.api.middleware.{CorsDirectives, ErrorHandlingDirectives, LoggingDirectives}
import com.aboveland.api.routes.Routes
import com.aboveland.api.services.HealthService
import com.aboveland.example.services.UserService
import com.aboveland.example.repository.{UserRepository, InMemoryUserRepository}
import com.aboveland.api.handlers.HealthHandler
import com.aboveland.example.handlers.UserHandler

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object HttpServer {
  
  def main(args: Array[String]): Unit = {
    startServer()
  }
  
  def startServerAsync()(implicit ec: ExecutionContext): Future[Unit] = {
    Future {
      startServer()
    }
  }
  
  def startServer(): Unit = {
    // Create Actor system first to get access to Akka logging
    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "world-api-system")
    implicit val ec: ExecutionContext = system.executionContext
    
    system.log.info("Starting HTTP server initialization")
    system.log.debug("Debug: Starting server initialization")
    
    // Load configuration
    system.log.debug("Loading application configuration")
    val appConfig = AppConfig.load()
    system.log.info("Application configuration loaded successfully")
    
    // Create service layer
    system.log.debug("Initializing service layer")
    val userRepository = new InMemoryUserRepository()
    val userService = new UserService(userRepository)
    val healthService = new HealthService()
    system.log.debug("Service layer initialized successfully")
    
    // Create handlers
    system.log.debug("Creating request handlers")
    val userHandler = new UserHandler(userService)
    val healthHandler = new HealthHandler(healthService)
    system.log.debug("Request handlers created successfully")
    
    // Create routes
    system.log.debug("Setting up HTTP routes")
    val routes = new Routes(userHandler, healthHandler)
    system.log.debug("HTTP routes configured successfully")
    
    // Combine all routes and middleware
    system.log.debug("Configuring middleware and combining routes")
    val allRoutes: Route = {
      CorsDirectives.corsHandler {
        ErrorHandlingDirectives.handleErrors {
          LoggingDirectives.logRequests {
            routes.allRoutes
          }
        }
      }
    }
    system.log.debug("Middleware and routes configured successfully")
    
    // Start HTTP server
    system.log.info("Starting HTTP server on {}:{}", appConfig.server.host, appConfig.server.port)
    val bindingFuture = Http().newServerAt(appConfig.server.host, appConfig.server.port)
      .bind(allRoutes)
    
    bindingFuture.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("HTTP server successfully started on {}:{}", address.getHostString, address.getPort)
        system.log.info("Server is ready to accept requests at http://{}:{}", address.getHostString, address.getPort)
        system.log.info("Press ENTER to stop the server...")
      case Failure(ex) =>
        system.log.error("Failed to start HTTP server on {}:{}", appConfig.server.host, appConfig.server.port)
        system.log.error("HTTP server startup failed", ex)
        system.terminate()
    }(ec)
    
    // Graceful shutdown hook
    sys.addShutdownHook {
      system.log.info("Received shutdown signal, initiating graceful shutdown...")
      bindingFuture
        .flatMap(_.unbind())(ec)
        .onComplete {
          case Success(_) =>
            system.log.info("HTTP server unbound successfully")
            system.log.info("Shutting down Actor system...")
            system.terminate()
            system.log.info("Server shutdown completed")
          case Failure(ex) =>
            system.log.warn("Error occurred during server unbinding")
            system.log.warn("Server unbinding failed", ex)
            system.log.info("Shutting down Actor system anyway...")
            system.terminate()
            system.log.info("Server shutdown completed")
        }(ec)
    }
    
    // Block main thread to keep server running
    try {
      // Use CountDownLatch to keep server running until shutdown signal received
      val latch = new java.util.concurrent.CountDownLatch(1)
      
      // Add graceful shutdown hook for the latch
      sys.addShutdownHook {
        system.log.debug("Shutdown hook triggered, releasing latch")
        latch.countDown()
      }
      
      system.log.debug("Server is running, waiting for shutdown signal...")
      latch.await()
      
    } catch {
      case _: InterruptedException =>
        system.log.info("Server was interrupted")
      case ex: Exception =>
        system.log.error("Unexpected error while keeping server running")
        system.log.error("Server runtime error", ex)
    }
  }
}
