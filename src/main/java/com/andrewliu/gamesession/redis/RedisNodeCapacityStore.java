package com.andrewliu.gamesession.redis;

import com.andrewliu.gamesession.model.GpuNode;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.store.type", havingValue = "redis")
public class RedisNodeCapacityStore implements NodeCapacityStore {

  private static final RedisScript<Long> RESERVE_SCRIPT = RedisScript.of("""
      local current = tonumber(redis.call('GET', KEYS[1]) or '0')
      local max = tonumber(ARGV[1])
      if current < max then
        return redis.call('INCR', KEYS[1])
      end
      return -1
      """, Long.class);

  private static final RedisScript<Long> RELEASE_SCRIPT = RedisScript.of("""
      local current = tonumber(redis.call('GET', KEYS[1]) or '0')
      if current <= 0 then
        redis.call('SET', KEYS[1], 0)
        return 0
      end
      return redis.call('DECR', KEYS[1])
      """, Long.class);

  private final StringRedisTemplate redisTemplate;

  public RedisNodeCapacityStore(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public boolean tryReserve(GpuNode node) {
    Long result = redisTemplate.execute(
        RESERVE_SCRIPT, List.of(key(node.getNodeId())), Integer.toString(node.getMaxSessions()));
    return result != null && result > 0;
  }

  @Override
  public int release(String nodeId) {
    Long result = redisTemplate.execute(RELEASE_SCRIPT, List.of(key(nodeId)));
    return result == null ? 0 : result.intValue();
  }

  @Override
  public void setActiveSessions(String nodeId, int activeSessions) {
    redisTemplate.opsForValue().set(key(nodeId), Integer.toString(Math.max(0, activeSessions)));
  }

  @Override
  public int getActiveSessions(String nodeId) {
    String value = redisTemplate.opsForValue().get(key(nodeId));
    return value == null ? 0 : Integer.parseInt(value);
  }

  private String key(String nodeId) {
    return "node:capacity:" + nodeId;
  }
}
