package com.aboveland.config

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.config.ConfigFactory

object AkkaConfig {
  
  // Load configuration
  val config = ConfigFactory.load()
  
  // Create a basic actor system configuration
  def createActorSystem[T](name: String, behavior: akka.actor.typed.Behavior[T]): ActorSystem[T] = {
    ActorSystem(behavior, name, config)
  }
  
  // Get Akka configuration
  def getAkkaConfig = config.getConfig("akka")
  
  // Get application-specific configuration
  def getAppConfig = config.getConfig("app")
}

