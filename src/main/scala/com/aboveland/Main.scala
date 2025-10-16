package com.aboveland

import com.aboveland.api.HttpServer
import scala.concurrent.ExecutionContext.Implicits.global

object Main {
  
  def main(args: Array[String]): Unit = {
    // Start HTTP server asynchronously
    HttpServer.startServerAsync().onComplete {
      case scala.util.Success(_) => 
        println("Server started successfully")
      case scala.util.Failure(exception) => 
        println(s"Failed to start server: ${exception.getMessage}")
        sys.exit(1)
    }
    
    // Keep the main thread alive
    Thread.currentThread().join()
  }
}

