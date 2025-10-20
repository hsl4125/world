package com.aboveland.models

/**
 * BaseServer represents a dedicated server instance
 * 
 * @param sid Unique BaseServer ID
 * @param k8sCid Unique K8s create ID
 * @param index Index number
 * @param machineId Unique Identification
 * @param serverType BaseServer Type (0: Instance, 1: Dungeon, 2: Pvp, 3: Pagoda)
 * @param status Dedicated BaseServer Status
 * @param mapName Map Name
 * @param ip Dedicated BaseServer IP
 * @param port Dedicated BaseServer Port
 * @param platform Platform (k8s, native, self)
 * @param zoneTag Zone tag, default: Zone.None
 * @param debug Debug String
 * @param department The server starting mode
 * @param dungeonPlayerMax The dungeon max player
 * @param dungeonToken The dungeon token, prevent dungeon reset, player enter in other's dungeon
 */
case class BaseServer(
  sid: Long,
  k8sCid: Long,
  index: Int,
  machineId: String,
  serverType: Int,
  status: Int,
  mapName: String,
  ip: String,
  port: Int,
  platform: String,
  zoneTag: String,
  debug: String,
  department: String,
  dungeonPlayerMax: Int,
  dungeonToken: String
)

/**
 * BaseServer Type Constants
 */
object BaseServerType {
  val INSTANCE: Int = 0
  val DUNGEON: Int = 1
  val PVP: Int = 2
  val PAGODA: Int = 3
}

/**
 * BaseServer Status Constants
 */
object BaseServerStatus {
  val STOPPED: Int = 0
  val STARTING: Int = 1
  val RUNNING: Int = 2
  val STOPPING: Int = 3
  val ERROR: Int = 4
}

/**
 * Platform Constants
 */
object Platform {
  val K8S: String = "k8s"
  val NATIVE: String = "native"
  val SELF: String = "self"
}

/**
 * Zone Constants
 */
object Zone {
  val NONE: String = "Zone.None"
}
