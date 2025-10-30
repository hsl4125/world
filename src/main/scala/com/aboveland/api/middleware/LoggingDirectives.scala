package com.aboveland.api.middleware

import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.{ContentType, MessageEntity}
import akka.stream.scaladsl.{Flow, Sink}
import akka.NotUsed
import akka.util.ByteString
import org.slf4j.LoggerFactory

import scala.concurrent.duration._

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

  // Logs headers and up to maxBytes of request/response bodies for textual/JSON content
  def logRequestResponseWithBody(maxBytes: Int = 8192, timeout: FiniteDuration = 2.seconds): Directive0 = {
    extractRequestContext.flatMap { ctx =>
      implicit val mat = ctx.materializer
      implicit val ec = ctx.executionContext

      def isTextual(ct: ContentType): Boolean =
        ct.mediaType.isText || ct.mediaType.subType == "json" || ct.mediaType.subType == "xml"

      val startTime = System.currentTimeMillis()

      onSuccess(ctx.request.entity.toStrict(timeout)).flatMap { strictReq =>
        val reqHeaders = ctx.request.headers.mkString(", ")
        val reqBody =
          if (strictReq.data.length > 0 && isTextual(strictReq.contentType))
            strictReq.data.decodeString(strictReq.contentType.charsetOption.map(_.value).getOrElse("UTF-8")).take(maxBytes)
          else ""

        logger.info(s"HTTP REQ ${ctx.request.method.value} ${ctx.request.uri} headers=[${reqHeaders}] body=${if (reqBody.nonEmpty) reqBody else "<omitted>"}")

        mapRequest(_.withEntity(strictReq)).tflatMap { _ =>
          mapResponse { resp =>
            val duration = System.currentTimeMillis() - startTime

            if (resp.entity.isKnownEmpty || !isTextual(resp.entity.contentType)) {
              logger.info(s"HTTP RES ${resp.status.intValue()} duration=${duration}ms headers=[${resp.headers.mkString(", ")}] body=<omitted>")
              resp
            } else {
              val accRef = new java.util.concurrent.atomic.AtomicReference[ByteString](ByteString.empty)
              val tee: Flow[ByteString, ByteString, NotUsed] =
                Flow[ByteString]
                  .alsoTo(Sink.foreach { bs =>
                    val current = accRef.get()
                    if (current.length < maxBytes) {
                      val next = current ++ bs.take(maxBytes - current.length)
                      accRef.set(next)
                    }
                  })
                  .watchTermination() { (_, done) =>
                    done.onComplete { _ =>
                      val charset = resp.entity.contentType.charsetOption.map(_.value).getOrElse("UTF-8")
                      val preview = accRef.get().decodeString(charset)
                      logger.info(s"HTTP RES DONE ${resp.status.intValue()} duration=${duration}ms headers=[${resp.headers.mkString(", ")}] body-preview=${preview}")
                    }(ec)
                    NotUsed
                  }

              val ct = resp.entity.contentType.toString()
              val cl = resp.entity.contentLengthOption.map(_.toString).getOrElse("unknown")
              logger.info(s"HTTP RES START ${resp.status.intValue()} headers=[${resp.headers.mkString(", ")}] content-type=${ct} content-length=${cl}")
              val loggedEntity: MessageEntity = resp.entity.transformDataBytes(tee).asInstanceOf[MessageEntity]
              resp.withEntity(loggedEntity)
            }
          }
        }
      }
    }
  }
}
