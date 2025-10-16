package com.aboveland.api.services

import scala.concurrent.Future
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

case class HealthStatus(
  status: String,
  timestamp: String,
  version: String,
  uptime: String
)

class HealthService {
  
  private val startTime = System.currentTimeMillis()
  private val version = "1.0.0"
  private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
  
  def getHealthStatus: Future[HealthStatus] = {
    val currentTime = LocalDateTime.now()
    val uptimeMs = System.currentTimeMillis() - startTime
    val uptime = formatUptime(uptimeMs)
    
    val healthStatus = HealthStatus(
      status = "UP",
      timestamp = currentTime.format(formatter),
      version = version,
      uptime = uptime
    )
    
    Future.successful(healthStatus)
  }
  
  private def formatUptime(uptimeMs: Long): String = {
    val seconds = uptimeMs / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    
    if (days > 0) {
      s"${days}d ${hours % 24}h ${minutes % 60}m ${seconds % 60}s"
    } else if (hours > 0) {
      s"${hours}h ${minutes % 60}m ${seconds % 60}s"
    } else if (minutes > 0) {
      s"${minutes}m ${seconds % 60}s"
    } else {
      s"${seconds}s"
    }
  }
}
