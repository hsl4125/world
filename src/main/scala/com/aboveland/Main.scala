package com.aboveland

import com.aboveland.api.HttpServer
import org.slf4j.LoggerFactory

object Main {
  
  private val logger = LoggerFactory.getLogger("com.aboveland.Main")
  
  def main(args: Array[String]): Unit = {
    logger.info("Starting World application...")
    
    try {
      HttpServer.startServer()
    } catch {
      case ex: Exception =>
        logger.error("Failed to start application", ex)
        sys.exit(1)
    }
  }
}

