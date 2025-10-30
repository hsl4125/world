package com.aboveland.api.middleware

import akka.http.scaladsl.model.{StatusCodes, HttpResponse, HttpEntity}
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.server.{Directive0, ExceptionHandler, RejectionHandler, MethodRejection, MissingFormFieldRejection, MissingQueryParamRejection, ValidationRejection, MalformedRequestContentRejection, UnsupportedRequestContentTypeRejection, RequestEntityExpectedRejection}
import akka.http.scaladsl.server.Directives._
import com.aboveland.api.models.{ErrorResponse, ApiResponse}
import spray.json._
import com.aboveland.api.models.ApiResponse._

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ErrorHandlingDirectives {
  
  val formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
  
  def handleErrors: Directive0 = {
    handleExceptions(exceptionHandler) & handleRejections(rejectionHandler)
  }
  
  private val exceptionHandler = ExceptionHandler {
    case ex: IllegalArgumentException =>
      complete(
        StatusCodes.BadRequest,
        HttpEntity(`application/json`, createErrorResponse("Invalid request", Some(Seq(ex.getMessage))))
      )
    
    case ex: NoSuchElementException =>
      complete(
        StatusCodes.NotFound,
        HttpEntity(`application/json`, createErrorResponse("Resource not found", Some(Seq(ex.getMessage))))
      )
    
    case ex: UnsupportedOperationException =>
      complete(
        StatusCodes.MethodNotAllowed,
        HttpEntity(`application/json`, createErrorResponse("Operation not supported", Some(Seq(ex.getMessage))))
      )
    
    case ex: Exception =>
      complete(
        StatusCodes.InternalServerError,
        HttpEntity(`application/json`, createErrorResponse("Internal server error", Some(Seq(ex.getMessage))))
      )
  }
  
  private val rejectionHandler = RejectionHandler.newBuilder()
    .handle {
      case MalformedRequestContentRejection(msg, _) =>
        complete(
          StatusCodes.BadRequest,
          HttpEntity(`application/json`, createErrorResponse("Invalid JSON body", Some(Seq(msg))))
        )
    }
    .handle {
      case ValidationRejection(msg, _) =>
        complete(
          StatusCodes.BadRequest,
          HttpEntity(`application/json`, createErrorResponse("Validation failed", Some(Seq(msg))))
        )
    }
    .handle {
      case UnsupportedRequestContentTypeRejection(supported) =>
        complete(
          StatusCodes.BadRequest,
          HttpEntity(`application/json`, createErrorResponse("Unsupported Content-Type", Some(Seq(s"Supported: ${supported.map(_.value).mkString(", ")}"))))
        )
    }
    .handle {
      case RequestEntityExpectedRejection =>
        complete(
          StatusCodes.BadRequest,
          HttpEntity(`application/json`, createErrorResponse("Request entity expected", None))
        )
    }
    .handleAll[MethodRejection] { mrs =>
      val methods = mrs.map(_.supported.name).distinct.sorted
      complete(
        StatusCodes.MethodNotAllowed,
        HttpEntity(`application/json`, createErrorResponse(s"Method not allowed. Allowed: ${methods.mkString(",")}", None))
      )
    }
    .handle {
      case MissingQueryParamRejection(param) =>
        complete(
          StatusCodes.BadRequest,
          HttpEntity(`application/json`, createErrorResponse(s"Missing query parameter: $param", None))
        )
    }
    .handle {
      case MissingFormFieldRejection(field) =>
        complete(
          StatusCodes.BadRequest,
          HttpEntity(`application/json`, createErrorResponse(s"Missing form field: $field", None))
        )
    }
    .handleNotFound {
      complete(
        StatusCodes.NotFound,
        HttpEntity(`application/json`, createErrorResponse("Not Found", None))
      )
    }
    .result()
  
  private def createErrorResponse(message: String, errors: Option[Seq[String]]): String = {
    val errorResponse = ErrorResponse(
      message = message,
      errors = errors,
      timestamp = LocalDateTime.now().format(formatter)
    )
    errorResponse.toJson.compactPrint
  }
}
