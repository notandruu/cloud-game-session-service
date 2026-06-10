package com.andrewliu.gamesession.service;

import com.andrewliu.gamesession.dto.MetricsResponse;
import com.andrewliu.gamesession.model.GpuNode;
import com.andrewliu.gamesession.model.SessionStatus;
import com.andrewliu.gamesession.redis.NodeCapacityStore;
import com.andrewliu.gamesession.repository.NodeRepository;
import com.andrewliu.gamesession.repository.SessionRepository;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {

  private final SessionRepository sessionRepository;
  private final NodeRepository nodeRepository;
  private final NodeCapacityStore capacityStore;
  private final OperationalMetrics operationalMetrics;

  public MetricsService(
      SessionRepository sessionRepository,
      NodeRepository nodeRepository,
      NodeCapacityStore capacityStore,
      OperationalMetrics operationalMetrics) {
    this.sessionRepository = sessionRepository;
    this.nodeRepository = nodeRepository;
    this.capacityStore = capacityStore;
    this.operationalMetrics = operationalMetrics;
  }

  public MetricsResponse metrics() {
    long activeSessions = sessionRepository.countByStatus(SessionStatus.ACTIVE);
    long queuedSessions = sessionRepository.countByStatus(SessionStatus.QUEUED);
    long totalCapacity = 0;
    long usedCapacity = 0;
    long registeredNodes = 0;

    for (GpuNode node : nodeRepository.findAll()) {
      registeredNodes++;
      totalCapacity += node.getMaxSessions();
      usedCapacity += capacityStore.getActiveSessions(node.getNodeId());
    }

    return new MetricsResponse(
        activeSessions,
        queuedSessions,
        registeredNodes,
        totalCapacity,
        usedCapacity,
        Math.max(0, totalCapacity - usedCapacity),
        operationalMetrics.allocationFailures(),
        operationalMetrics.expiredSessions(),
        operationalMetrics.averageQueueWaitMillis());
  }
}
