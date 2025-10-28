package com.aboveland.models

import java.time.Instant

case class ServerState(
  baseInfo: BaseServer,

  pendingStatus: Int,
  lastTime: Instant,
  enterPendingTime: Instant,
  players: Int,
  running: Boolean,
  k8sDestroy: Boolean
)