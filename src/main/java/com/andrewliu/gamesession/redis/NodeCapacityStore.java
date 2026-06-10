package com.andrewliu.gamesession.redis;

import com.andrewliu.gamesession.model.GpuNode;

public interface NodeCapacityStore {

  boolean tryReserve(GpuNode node);

  int release(String nodeId);

  void setActiveSessions(String nodeId, int activeSessions);

  int getActiveSessions(String nodeId);
}
