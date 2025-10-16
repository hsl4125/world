package com.aboveland

import com.aboveland.api.HttpServer
import scala.concurrent.ExecutionContext.Implicits.global
import java.io.File
import org.slf4j.LoggerFactory

object Main {
  
  private val logger = LoggerFactory.getLogger("com.aboveland.Main")
  
  def ensureLogDirectoryExists(): Unit = {
    val logDir = new File("logs")
    if (!logDir.exists()) {
      logDir.mkdirs()
      logger.info(s"Created log directory: ${logDir.getAbsolutePath}")
    } else {
      logger.debug(s"Log directory already exists: ${logDir.getAbsolutePath}")
    }
  }
  
  def main(args: Array[String]): Unit = {
    // 确保日志目录存在
    ensureLogDirectoryExists()
    
    logger.info("Starting World application...")
    logger.debug("Debug message to test file logging")
    // Start HTTP server asynchronously
    HttpServer.startServerAsync().onComplete {
      case scala.util.Success(_) => 
        logger.info("Server started successfully")
        println("Server started successfully")
      case scala.util.Failure(exception) => 
        logger.error(s"Failed to start server: ${exception.getMessage}", exception)
        println(s"Failed to start server: ${exception.getMessage}")
        sys.exit(1)
    }
    
    // Keep the main thread alive
    Thread.currentThread().join()
  }
}

