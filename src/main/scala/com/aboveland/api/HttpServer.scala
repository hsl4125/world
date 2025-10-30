package com.aboveland.api

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import com.aboveland.api.config.AppConfig
import com.aboveland.api.middleware.{CorsDirectives, ErrorHandlingDirectives, LoggingDirectives}
import com.aboveland.api.routes.Routes
import com.aboveland.api.services.{HealthService, WorldService}
import com.aboveland.api.handlers.{HealthHandler, WorldHandler}
import com.aboveland.actors.DedicatedServerManager

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
      val routes = createApplicationRoutes()(ec, system)
      
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
  
  private def createApplicationRoutes()(implicit ec: ExecutionContext, system: ActorSystem[Nothing]): Route = {
    // Create all application components
    val healthService = new HealthService()

    
    // Create DedicatedServerManager Actor
    val dedicatedServerManager = system.systemActorOf(DedicatedServerManager(), "dedicated-server-manager")
    val worldService = new WorldService(dedicatedServerManager)

    val healthHandler = new HealthHandler(healthService)
    val worldHandler = new WorldHandler(worldService)
    val routes = new Routes(healthHandler, worldHandler)
    
    // Combine middleware and routes
    CorsDirectives.corsHandler {
      ErrorHandlingDirectives.handleErrors {
        LoggingDirectives.logRequestResponseWithBody() {
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
