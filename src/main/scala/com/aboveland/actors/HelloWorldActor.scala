package com.aboveland.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object HelloWorldActor {
  
  // Define the message protocol
  sealed trait Command
  case class Greet(name: String) extends Command
  case class GreetBack(replyTo: ActorRef[String]) extends Command
  
  // Define the actor behavior
  def apply(): Behavior[Command] = Behaviors.receive { (context, message) =>
    message match {
      case Greet(name) =>
        context.log.info(s"Hello, $name!")
        Behaviors.same
        
      case GreetBack(replyTo) =>
        replyTo ! "Hello back!"
        Behaviors.same
    }
  }
}

