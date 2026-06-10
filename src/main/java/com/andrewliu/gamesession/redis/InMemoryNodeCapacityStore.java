package com.andrewliu.gamesession.redis;

import com.andrewliu.gamesession.model.GpuNode;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.store.type", havingValue = "memory", matchIfMissing = true)
public class InMemoryNodeCapacityStore implements NodeCapacityStore {

  private final ConcurrentMap<String, Integer> activeSessions = new ConcurrentHashMap<>();

  @Override
  public synchronized boolean tryReserve(GpuNode node) {
    int current = getActiveSessions(node.getNodeId());
    if (current >= node.getMaxSessions()) {
      return false;
    }
    activeSessions.put(node.getNodeId(), current + 1);
    return true;
  }

  @Override
  public synchronized int release(String nodeId) {
    int current = getActiveSessions(nodeId);
    int next = Math.max(0, current - 1);
    activeSessions.put(nodeId, next);
    return next;
  }

  @Override
  public void setActiveSessions(String nodeId, int count) {
    activeSessions.put(nodeId, Math.max(0, count));
  }

  @Override
  public int getActiveSessions(String nodeId) {
    return activeSessions.getOrDefault(nodeId, 0);
  }
}
