package com.aboveland.api.utils

import spray.json.{DefaultJsonProtocol, JsonFormat}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object JsonSupport extends DefaultJsonProtocol {
  
  // JSON serialization support for LocalDateTime
  implicit val localDateTimeFormat: JsonFormat[LocalDateTime] = new JsonFormat[LocalDateTime] {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    def write(obj: LocalDateTime): spray.json.JsValue = {
      spray.json.JsString(obj.format(formatter))
    }
    
    def read(json: spray.json.JsValue): LocalDateTime = json match {
      case spray.json.JsString(s) => LocalDateTime.parse(s, formatter)
      case _ => throw new RuntimeException("Expected string for LocalDateTime")
    }
  }
}
