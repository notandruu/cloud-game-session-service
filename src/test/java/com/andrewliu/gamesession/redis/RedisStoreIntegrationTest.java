package com.andrewliu.gamesession.redis;

import static org.assertj.core.api.Assertions.assertThat;

import com.andrewliu.gamesession.model.GpuNode;
import com.andrewliu.gamesession.model.NodeStatus;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class RedisStoreIntegrationTest {

  @Container
  static final GenericContainer<?> REDIS =
      new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

  private LettuceConnectionFactory connectionFactory;
  private StringRedisTemplate redisTemplate;

  @BeforeEach
  void setUp() {
    connectionFactory = new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(6379));
    connectionFactory.afterPropertiesSet();
    redisTemplate = new StringRedisTemplate(connectionFactory);
    redisTemplate.afterPropertiesSet();
  }

  @AfterEach
  void tearDown() {
    redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    connectionFactory.destroy();
  }

  @Test
  void regionQueueIsFifoAndTracksPosition() {
    RedisRegionQueueStore queueStore = new RedisRegionQueueStore(redisTemplate);

    queueStore.enqueue("us-west", "sess_a");
    queueStore.enqueue("us-west", "sess_b");

    assertThat(queueStore.position("us-west", "sess_a")).isEqualTo(1);
    assertThat(queueStore.position("us-west", "sess_b")).isEqualTo(2);
    assertThat(queueStore.pop("us-west")).contains("sess_a");
    assertThat(queueStore.pop("us-west")).contains("sess_b");
  }

  @Test
  void capacityReservationIsAtomicAndBounded() {
    RedisNodeCapacityStore capacityStore = new RedisNodeCapacityStore(redisTemplate);
    GpuNode node = new GpuNode(
        "gpu-node-usw-1",
        "us-west",
        1,
        0,
        NodeStatus.AVAILABLE,
        Instant.now(),
        Instant.now());

    assertThat(capacityStore.tryReserve(node)).isTrue();
    assertThat(capacityStore.tryReserve(node)).isFalse();
    assertThat(capacityStore.getActiveSessions(node.getNodeId())).isEqualTo(1);
    assertThat(capacityStore.release(node.getNodeId())).isEqualTo(0);
  }
}
