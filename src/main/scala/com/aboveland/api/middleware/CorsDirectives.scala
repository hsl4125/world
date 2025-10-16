package com.aboveland.api.middleware

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}

object CorsDirectives {
  
  def corsHandler(routes: Route): Route = {
    cors(routes)
  }
  
  def cors: Directive0 = {
    respondWithHeaders(
      `Access-Control-Allow-Origin`.*,
      `Access-Control-Allow-Credentials`(true),
      `Access-Control-Allow-Headers`("Content-Type", "Authorization", "X-Requested-With", "Accept", "Origin"),
      `Access-Control-Allow-Methods`(GET, POST, PUT, DELETE, OPTIONS)
    )
  }
}
