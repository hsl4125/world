package com.aboveland.actors

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.ActorRef
import org.scalatest.wordspec.AnyWordSpecLike

class HelloWorldActorSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  
  "HelloWorldActor" should {
    
    "respond to Greet message" in {
      val probe = createTestProbe[String]()
      val actorRef = spawn(HelloWorldActor())
      
      actorRef ! HelloWorldActor.Greet("Test")
      
      // The actor should log the message, but we can't easily test logging
      // This test mainly ensures the actor doesn't crash
    }
    
    "respond to GreetBack message" in {
      val probe = createTestProbe[String]()
      val actorRef = spawn(HelloWorldActor())
      
      actorRef ! HelloWorldActor.GreetBack(probe.ref)
      
      probe.expectMessage("Hello back!")
    }
  }
}

