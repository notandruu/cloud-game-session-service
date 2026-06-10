package com.andrewliu.gamesession.service;

import com.andrewliu.gamesession.config.AppProperties;
import com.andrewliu.gamesession.model.GpuNode;
import com.andrewliu.gamesession.model.NodeStatus;
import com.andrewliu.gamesession.model.Session;
import com.andrewliu.gamesession.model.SessionStatus;
import com.andrewliu.gamesession.redis.ActiveSessionStore;
import com.andrewliu.gamesession.redis.NodeCapacityStore;
import com.andrewliu.gamesession.repository.SessionRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AllocationService {

  private static final Logger log = LoggerFactory.getLogger(AllocationService.class);

  private final NodeService nodeService;
  private final SessionRepository sessionRepository;
  private final ActiveSessionStore activeSessionStore;
  private final NodeCapacityStore capacityStore;
  private final AppProperties properties;
  private final OperationalMetrics operationalMetrics;
  private final Clock clock;

  public AllocationService(
      NodeService nodeService,
      SessionRepository sessionRepository,
      ActiveSessionStore activeSessionStore,
      NodeCapacityStore capacityStore,
      AppProperties properties,
      OperationalMetrics operationalMetrics,
      Clock clock) {
    this.nodeService = nodeService;
    this.sessionRepository = sessionRepository;
    this.activeSessionStore = activeSessionStore;
    this.capacityStore = capacityStore;
    this.properties = properties;
    this.operationalMetrics = operationalMetrics;
    this.clock = clock;
  }

  public Optional<Session> tryActivate(Session session) {
    Instant now = clock.instant();
    return nodeService.listByRegion(session.getRegion()).stream()
        .filter(node -> node.getStatus() != NodeStatus.OFFLINE)
        .sorted(Comparator.comparingInt(GpuNode::getActiveSessions)
            .thenComparing(GpuNode::getNodeId))
        .filter(node -> node.getActiveSessions() < node.getMaxSessions())
        .filter(capacityStore::tryReserve)
        .findFirst()
        .map(node -> activate(session, node, now));
  }

  public String release(Session session) {
    if (session.getNodeId() == null || session.getNodeId().isBlank()) {
      return null;
    }
    int activeSessions = capacityStore.release(session.getNodeId());
    GpuNode node = nodeService.getRequired(session.getNodeId());
    node.setActiveSessions(activeSessions);
    node.setUpdatedAt(clock.instant());
    nodeService.save(node);
    activeSessionStore.delete(session.getSessionId());
    log.info(
        "gpu_node_capacity_released sessionId={} nodeId={} activeSessions={}",
        session.getSessionId(),
        session.getNodeId(),
        activeSessions);
    return session.getNodeId();
  }

  private Session activate(Session session, GpuNode node, Instant now) {
    node.setActiveSessions(capacityStore.getActiveSessions(node.getNodeId()));
    node.setUpdatedAt(now);
    nodeService.save(node);

    session.setNodeId(node.getNodeId());
    session.transitionTo(SessionStatus.ACTIVE, now);
    session.setLastHeartbeatAt(now);
    session.setUpdatedAt(now);
    Session saved = sessionRepository.save(session);
    activeSessionStore.save(saved, properties.getActiveSessionTtl());

    if (saved.getQueueEnteredAt() != null) {
      operationalMetrics.recordQueueWait(Duration.between(saved.getQueueEnteredAt(), now));
    }

    log.info(
        "session_allocated sessionId={} userId={} gameId={} region={} nodeId={}",
        saved.getSessionId(),
        saved.getUserId(),
        saved.getGameId(),
        saved.getRegion(),
        saved.getNodeId());
    return saved;
  }

  public void recordAllocationFailure(String sessionId, String region) {
    operationalMetrics.recordAllocationFailure();
    log.warn("session_allocation_failed sessionId={} region={}", sessionId, region);
  }
}
