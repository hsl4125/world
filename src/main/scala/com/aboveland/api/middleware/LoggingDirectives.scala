package com.aboveland.api.middleware

import akka.http.scaladsl.server.{Directive0, Directive1}
import akka.http.scaladsl.server.Directives._
import akka.event.Logging
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.server.Route
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success}

object LoggingDirectives {
  
  private val logger = LoggerFactory.getLogger("com.aboveland.api.middleware.LoggingDirectives")
  
  def logRequests: Directive0 = {
    extractRequestContext.flatMap { ctx =>
      val startTime = System.currentTimeMillis()
      
      mapResponse { response =>
        val duration = System.currentTimeMillis() - startTime
        logger.info(
          s"${ctx.request.method.value} ${ctx.request.uri.path} " +
          s"${response.status.intValue()} ${duration}ms"
        )
        response
      }
    }
  }
  
  def logRequestAndResponse: Directive0 = {
    extractRequestContext.flatMap { ctx =>
      val startTime = System.currentTimeMillis()
      
      logger.info(s"Incoming request: ${ctx.request.method.value} ${ctx.request.uri}")
      
      mapResponse { response =>
        val duration = System.currentTimeMillis() - startTime
        logger.info(
          s"Response: ${response.status.intValue()} " +
          s"Duration: ${duration}ms " +
          s"for ${ctx.request.method.value} ${ctx.request.uri.path}"
        )
        response
      }
    }
  }
}
