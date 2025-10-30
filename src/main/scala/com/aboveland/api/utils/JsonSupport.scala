package com.aboveland.api.utils

import spray.json.{DefaultJsonProtocol, JsonFormat, RootJsonFormat}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.aboveland.models.{BaseServer, PlayerNumber, ErrorResponse}
import com.aboveland.actors.DedicatedServerManager

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
  
  // Model JSON protocols
  implicit val baseServerFormat: RootJsonFormat[BaseServer] = 
    jsonFormat15(BaseServer.apply)
  
  implicit val registerServerResponseFormat: RootJsonFormat[DedicatedServerManager.RegisterServerResponse] = 
    jsonFormat1(DedicatedServerManager.RegisterServerResponse.apply)

  // Player number and update response formats
  implicit val playerNumberFormat: RootJsonFormat[PlayerNumber] =
    jsonFormat2(PlayerNumber.apply)

  implicit val modelErrorResponseFormat: RootJsonFormat[ErrorResponse] =
    jsonFormat2(ErrorResponse.apply)

  implicit val updatePlayerNumberResponseFormat: RootJsonFormat[DedicatedServerManager.UpdatePlayerNumberResponse] =
    jsonFormat1(DedicatedServerManager.UpdatePlayerNumberResponse.apply)
}
