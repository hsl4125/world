package com.aboveland.api.services

import akka.actor.typed.{ActorRef, ActorSystem}
import com.aboveland.actors.DedicatedServerManagerActor

import scala.concurrent.{ExecutionContext, Future}

class WorldService(private val managerRef: ActorRef[DedicatedServerManagerActor.Command])
                  (implicit system: ActorSystem[_], ec: ExecutionContext) {
  
  def getWorld(): Future[String] = {
    Future.successful("World data retrieved successfully")
  }
  
  def registerWorld(): Future[String] = {
    Future.successful("World registration completed")
  }
  
  def getWorldNumber(): Future[String] = {
    Future.successful("World number: 1")
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
