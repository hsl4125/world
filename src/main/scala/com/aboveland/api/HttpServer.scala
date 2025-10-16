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
import org.slf4j.LoggerFactory

object HttpServer {
  
  private val logger = LoggerFactory.getLogger("com.aboveland.api.HttpServer")
  
  def main(args: Array[String]): Unit = {
    startServer()
  }
  
  def startServerAsync()(implicit ec: ExecutionContext): Future[Unit] = {
    Future {
      startServer()
    }
  }
  
  def startServer(): Unit = {
    logger.info("Initializing HTTP server...")
    logger.debug("Debug: Starting server initialization")
    
    // Create Actor system
    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "yyy-api-system")
    implicit val ec: ExecutionContext = system.executionContext
    
    logger.info("Actor system created successfully")
    
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
        logger.info(s"HTTP server successfully bound to ${address.getHostString}:${address.getPort}")
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
        println(s"🚀 Server is running at http://${address.getHostString}:${address.getPort}")
        println("📝 Press ENTER to stop the server...")
      case Failure(ex) =>
        logger.error(s"Failed to bind HTTP server: ${ex.getMessage}", ex)
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }(ec)
    
    // Graceful shutdown hook
    sys.addShutdownHook {
      logger.info("Received shutdown signal, shutting down server...")
      system.log.info("Shutting down server...")
      bindingFuture
        .flatMap(_.unbind())(ec)
        .onComplete(_ => {
          logger.info("Server shutdown completed")
          system.terminate()
        })(ec)
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
