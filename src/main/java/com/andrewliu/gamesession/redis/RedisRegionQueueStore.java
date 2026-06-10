package com.andrewliu.gamesession.redis;

import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.store.type", havingValue = "redis")
public class RedisRegionQueueStore implements RegionQueueStore {

  private final StringRedisTemplate redisTemplate;

  public RedisRegionQueueStore(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public void enqueue(String region, String sessionId) {
    remove(region, sessionId);
    redisTemplate.opsForList().rightPush(key(region), sessionId);
  }

  @Override
  public void requeueFront(String region, String sessionId) {
    remove(region, sessionId);
    redisTemplate.opsForList().leftPush(key(region), sessionId);
  }

  @Override
  public Optional<String> pop(String region) {
    return Optional.ofNullable(redisTemplate.opsForList().leftPop(key(region)));
  }

  @Override
  public void remove(String region, String sessionId) {
    redisTemplate.opsForList().remove(key(region), 0, sessionId);
  }

  @Override
  public int position(String region, String sessionId) {
    List<String> values = redisTemplate.opsForList().range(key(region), 0, -1);
    if (values == null) {
      return -1;
    }
    int index = values.indexOf(sessionId);
    return index < 0 ? -1 : index + 1;
  }

  @Override
  public long size(String region) {
    Long size = redisTemplate.opsForList().size(key(region));
    return size == null ? 0 : size;
  }

  private String key(String region) {
    return "queue:region:" + region;
  }
}
