package com.aboveland.api.services

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.typed.scaladsl.ActorFlow
import akka.util.Timeout
import com.aboveland.actors.DedicatedServerManager
import com.aboveland.models.{BaseServer, PlayerNumber}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class WorldService(private val managerRef: ActorRef[DedicatedServerManager.Command])
                  (implicit system: ActorSystem[_], ec: ExecutionContext) {
  
  implicit val timeout: Timeout = Timeout(5.seconds)

  def getWorld(): Future[String] = {
    Future.successful("World data retrieved successfully")
  }
  
  def registerWorld(server: BaseServer): Future[BaseServer] = {
    // Create a Flow using ActorFlow.ask to send RegisterServer commands to DedicatedServerManager
    val registerServerFlow = ActorFlow.ask(managerRef) {
      (server: BaseServer, replyTo: ActorRef[DedicatedServerManager.RegisterServerResponse]) =>
        DedicatedServerManager.RegisterServer(server, replyTo)
    }

    // Use ActorFlow.ask to send RegisterServer command to DedicatedServerManager
    // Convert the single server to a Source, process through Flow, then convert to Future
    Source.single(server)
      .via(registerServerFlow)
      .runWith(Sink.head)
      .map(_.server)
  }
  
  def updatePlayerNumber(number: PlayerNumber): Future[DedicatedServerManager.UpdatePlayerNumberResponse] = {
    val updatePlayerNumberFlow = ActorFlow.ask(managerRef) {
      (num: PlayerNumber, replyTo: ActorRef[DedicatedServerManager.UpdatePlayerNumberResponse]) =>
        DedicatedServerManager.UpdatePlayerNumber(num, replyTo)
    }

    Source.single(number)
      .via(updatePlayerNumberFlow)
      .runWith(Sink.head)
  }
  
  def getWorldPortal(): Future[String] = {
    Future.successful("World portal status: Active")
  }
  
  def getDungeonMax(): Future[String] = {
    Future.successful("Max dungeons: 10")
  }
  
  def checkDSState(): Future[String] = {
    Future.successful("DS State: Connected")
  }
}
