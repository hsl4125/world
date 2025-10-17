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
import com.aboveland.example.repository.InMemoryUserRepository
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
    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "world-api-system")
    implicit val ec: ExecutionContext = system.executionContext
    
    system.log.info("Starting HTTP server initialization")
    
    try {
      val appConfig = AppConfig.load()
      val routes = createApplicationRoutes()
    
      system.log.info("Starting HTTP server on {}:{}", appConfig.server.host, appConfig.server.port)
      val bindingFuture = Http().newServerAt(appConfig.server.host, appConfig.server.port)
        .bind(routes)
      
      bindingFuture.onComplete {
        case Success(binding) =>
          val address = binding.localAddress
          system.log.info("Server started on http://{}:{}", address.getHostString, address.getPort)
        case Failure(ex) =>
          system.log.error("Failed to start server", ex)
          system.terminate()
      }(ec)
    
      setupGracefulShutdown(bindingFuture, system, ec)
      keepServerRunning()
      
    } catch {
      case ex: Exception =>
        system.log.error("Server initialization failed", ex)
        system.terminate()
        throw ex
    }
  }
  
  private def createApplicationRoutes()(implicit ec: ExecutionContext): Route = {
    // Create all application components
    val userRepository = new InMemoryUserRepository()
    val userService = new UserService(userRepository)
    val healthService = new HealthService()
    val userHandler = new UserHandler(userService)
    val healthHandler = new HealthHandler(healthService)
    val routes = new Routes(userHandler, healthHandler)
    
    // Combine middleware and routes
    CorsDirectives.corsHandler {
      ErrorHandlingDirectives.handleErrors {
        LoggingDirectives.logRequests {
          routes.allRoutes
        }
      }
    }
  }
  
  private def setupGracefulShutdown(bindingFuture: Future[Http.ServerBinding], system: ActorSystem[Nothing], ec: ExecutionContext): Unit = {
    sys.addShutdownHook {
      system.log.info("Shutting down server...")
      bindingFuture
        .flatMap(_.unbind())(ec)
        .onComplete(_ => system.terminate())(ec)
    }
  }
  
  private def keepServerRunning(): Unit = {
    val latch = new java.util.concurrent.CountDownLatch(1)
    sys.addShutdownHook(latch.countDown())
    latch.await()
  }
}
