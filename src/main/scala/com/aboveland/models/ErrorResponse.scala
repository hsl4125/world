package com.aboveland.models

object ErrorCode{
  val OK: Int = 200

  val ERROR_GET_SERVER: Int = 600
  val ERROR_REGISTER: Int = 601
  val ERROR_NUMBERS: Int = 603
  val ERROR_PORTAL: Int = 604
  val ERROR_NOT_Find_Map: Int = 1001
}

case class ErrorResponse(
  code: Int,
  message: String,
)
