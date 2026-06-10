package com.andrewliu.gamesession.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.andrewliu.gamesession.config.AppProperties;
import com.andrewliu.gamesession.dto.CreateSessionRequest;
import com.andrewliu.gamesession.dto.RegisterNodeRequest;
import com.andrewliu.gamesession.dto.SessionResponse;
import com.andrewliu.gamesession.dto.TerminateSessionResponse;
import com.andrewliu.gamesession.model.SessionStatus;
import com.andrewliu.gamesession.redis.InMemoryActiveSessionStore;
import com.andrewliu.gamesession.redis.InMemoryNodeCapacityStore;
import com.andrewliu.gamesession.redis.InMemoryRegionQueueStore;
import com.andrewliu.gamesession.repository.InMemoryNodeRepository;
import com.andrewliu.gamesession.repository.InMemorySessionRepository;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SessionServiceTest {

  private AppProperties properties;
  private MutableClock clock;
  private InMemorySessionRepository sessionRepository;
  private InMemoryNodeRepository nodeRepository;
  private InMemoryNodeCapacityStore capacityStore;
  private NodeService nodeService;
  private SessionService sessionService;

  @BeforeEach
  void setUp() {
    properties = new AppProperties();
    properties.setHeartbeatTimeout(Duration.ofSeconds(30));
    properties.setActiveSessionTtl(Duration.ofSeconds(60));
    clock = new MutableClock(Instant.parse("2026-06-09T10:00:00Z"));
    sessionRepository = new InMemorySessionRepository();
    nodeRepository = new InMemoryNodeRepository();
    InMemoryActiveSessionStore activeStore = new InMemoryActiveSessionStore();
    InMemoryRegionQueueStore queueStore = new InMemoryRegionQueueStore();
    capacityStore = new InMemoryNodeCapacityStore();
    OperationalMetrics metrics = new OperationalMetrics();
    nodeService = new NodeService(nodeRepository, capacityStore, clock);
    AllocationService allocationService =
        new AllocationService(nodeService, sessionRepository, activeStore, capacityStore, properties, metrics, clock);
    QueueService queueService =
        new QueueService(queueStore, sessionRepository, nodeRepository, allocationService, clock);
    sessionService =
        new SessionService(sessionRepository, activeStore, allocationService, queueService, properties, metrics, clock);
  }

  @Test
  void createsActiveSessionWhenCapacityExists() {
    nodeService.register(new RegisterNodeRequest("gpu-node-usw-1", "us-west", 2));

    SessionResponse response = sessionService.create(
        new CreateSessionRequest("user_123", "cyberpunk-2077", "us-west"));

    assertThat(response.status()).isEqualTo(SessionStatus.ACTIVE);
    assertThat(response.nodeId()).isEqualTo("gpu-node-usw-1");
    assertThat(response.queuePosition()).isNull();
    assertThat(capacityStore.getActiveSessions("gpu-node-usw-1")).isEqualTo(1);
  }

  @Test
  void queuesWhenFullAndPromotesNextSessionAfterTermination() {
    nodeService.register(new RegisterNodeRequest("gpu-node-usw-1", "us-west", 1));
    SessionResponse first = sessionService.create(
        new CreateSessionRequest("user_123", "cyberpunk-2077", "us-west"));
    SessionResponse second = sessionService.create(
        new CreateSessionRequest("user_456", "fortnite", "us-west"));

    assertThat(first.status()).isEqualTo(SessionStatus.ACTIVE);
    assertThat(second.status()).isEqualTo(SessionStatus.QUEUED);
    assertThat(second.queuePosition()).isEqualTo(1);

    TerminateSessionResponse terminated = sessionService.terminate(first.sessionId());
    SessionResponse promoted = sessionService.get(second.sessionId());

    assertThat(terminated.releasedNodeId()).isEqualTo("gpu-node-usw-1");
    assertThat(promoted.status()).isEqualTo(SessionStatus.ACTIVE);
    assertThat(promoted.nodeId()).isEqualTo("gpu-node-usw-1");
    assertThat(capacityStore.getActiveSessions("gpu-node-usw-1")).isEqualTo(1);
  }

  @Test
  void heartbeatUpdatesLastHeartbeatAt() {
    nodeService.register(new RegisterNodeRequest("gpu-node-usw-1", "us-west", 1));
    SessionResponse created = sessionService.create(
        new CreateSessionRequest("user_123", "cyberpunk-2077", "us-west"));

    clock.advance(Duration.ofSeconds(5));
    var heartbeat = sessionService.heartbeat(created.sessionId());

    assertThat(heartbeat.status()).isEqualTo(SessionStatus.ACTIVE);
    assertThat(heartbeat.lastHeartbeatAt()).isEqualTo(Instant.parse("2026-06-09T10:00:05Z"));
  }

  @Test
  void expiresStaleSessionAndDrainsQueue() {
    nodeService.register(new RegisterNodeRequest("gpu-node-usw-1", "us-west", 1));
    SessionResponse first = sessionService.create(
        new CreateSessionRequest("user_123", "cyberpunk-2077", "us-west"));
    SessionResponse second = sessionService.create(
        new CreateSessionRequest("user_456", "fortnite", "us-west"));

    clock.advance(Duration.ofSeconds(31));
    int expired = sessionService.expireStaleSessions();

    assertThat(expired).isEqualTo(1);
    assertThat(sessionService.get(first.sessionId()).status()).isEqualTo(SessionStatus.EXPIRED);
    assertThat(sessionService.get(second.sessionId()).status()).isEqualTo(SessionStatus.ACTIVE);
    assertThat(capacityStore.getActiveSessions("gpu-node-usw-1")).isEqualTo(1);
  }
}
