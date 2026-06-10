package com.andrewliu.gamesession.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.andrewliu.gamesession.config.AppProperties;
import com.andrewliu.gamesession.dto.RegisterNodeRequest;
import com.andrewliu.gamesession.model.Session;
import com.andrewliu.gamesession.redis.InMemoryActiveSessionStore;
import com.andrewliu.gamesession.redis.InMemoryNodeCapacityStore;
import com.andrewliu.gamesession.repository.InMemoryNodeRepository;
import com.andrewliu.gamesession.repository.InMemorySessionRepository;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class AllocationServiceTest {

  @Test
  void doesNotOverAllocateNodeCapacity() {
    MutableClock clock = new MutableClock(Instant.parse("2026-06-09T10:00:00Z"));
    AppProperties properties = new AppProperties();
    properties.setActiveSessionTtl(Duration.ofSeconds(60));
    InMemorySessionRepository sessionRepository = new InMemorySessionRepository();
    InMemoryNodeRepository nodeRepository = new InMemoryNodeRepository();
    InMemoryActiveSessionStore activeStore = new InMemoryActiveSessionStore();
    InMemoryNodeCapacityStore capacityStore = new InMemoryNodeCapacityStore();
    NodeService nodeService = new NodeService(nodeRepository, capacityStore, clock);
    OperationalMetrics metrics = new OperationalMetrics();
    AllocationService allocationService =
        new AllocationService(nodeService, sessionRepository, activeStore, capacityStore, properties, metrics, clock);
    nodeService.register(new RegisterNodeRequest("gpu-node-usw-1", "us-west", 1));

    Session first = Session.allocating("sess_first123", "user_123", "cyberpunk-2077", "us-west", clock.instant());
    Session second = Session.allocating("sess_second123", "user_456", "fortnite", "us-west", clock.instant());

    assertThat(allocationService.tryActivate(first)).isPresent();
    assertThat(allocationService.tryActivate(second)).isEmpty();
    assertThat(capacityStore.getActiveSessions("gpu-node-usw-1")).isEqualTo(1);
  }
}
