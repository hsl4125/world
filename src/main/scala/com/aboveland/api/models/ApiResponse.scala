package com.aboveland.api.models

import spray.json.DefaultJsonProtocol

// Common API response model
case class ApiResponse[T](
  success: Boolean,
  message: String,
  data: Option[T] = None,
  errors: Option[Seq[String]] = None
)

// Paginated response model
case class PagedResponse[T](
  success: Boolean,
  message: String,
  data: Seq[T],
  pagination: PaginationInfo
)

// Pagination information
case class PaginationInfo(
  page: Int,
  pageSize: Int,
  totalCount: Long,
  totalPages: Int
)

// Error response model
case class ErrorResponse(
  success: Boolean = false,
  message: String,
  errors: Option[Seq[String]] = None,
  timestamp: String
)

// JSON serialization support
object ApiResponse extends DefaultJsonProtocol {
  import spray.json._
  import com.aboveland.api.utils.JsonSupport._
  
  implicit def apiResponseFormat[T: JsonFormat]: JsonFormat[ApiResponse[T]] = 
    new JsonFormat[ApiResponse[T]] {
      def write(obj: ApiResponse[T]): JsValue = {
        JsObject(
          "success" -> JsBoolean(obj.success),
          "message" -> JsString(obj.message),
          "data" -> obj.data.map(_.toJson).getOrElse(JsNull),
          "errors" -> obj.errors.map(errs => JsArray(errs.map(JsString(_)).toVector)).getOrElse(JsNull)
        )
      }
      
      def read(json: JsValue): ApiResponse[T] = json match {
        case JsObject(fields) =>
          ApiResponse[T](
            success = fields.get("success").map(_.convertTo[Boolean]).getOrElse(false),
            message = fields.get("message").map(_.convertTo[String]).getOrElse(""),
            data = fields.get("data").filter(_ != JsNull).map(_.convertTo[T]),
            errors = fields.get("errors").filter(_ != JsNull).map(_.convertTo[Seq[String]])
          )
        case _ => throw new RuntimeException("Expected JSON object for ApiResponse")
      }
    }
    
  implicit def pagedResponseFormat[T: JsonFormat]: JsonFormat[PagedResponse[T]] = 
    jsonFormat4(PagedResponse.apply[T])
  implicit val paginationInfoFormat: JsonFormat[PaginationInfo] = jsonFormat4(PaginationInfo.apply)
  implicit val errorResponseFormat: JsonFormat[ErrorResponse] = jsonFormat4(ErrorResponse.apply)
}
