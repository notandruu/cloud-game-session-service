package com.andrewliu.gamesession.redis;

import com.andrewliu.gamesession.model.Session;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.store.type", havingValue = "redis")
public class RedisActiveSessionStore implements ActiveSessionStore {

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  public RedisActiveSessionStore(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
  }

  @Override
  public void save(Session session, Duration ttl) {
    redisTemplate.opsForValue().set(key(session.getSessionId()), serialize(session), ttl);
  }

  @Override
  public Optional<Session> find(String sessionId) {
    String payload = redisTemplate.opsForValue().get(key(sessionId));
    if (payload == null) {
      return Optional.empty();
    }
    try {
      return Optional.of(objectMapper.readValue(payload, Session.class));
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException("Unable to deserialize active session " + sessionId, ex);
    }
  }

  @Override
  public void refreshTtl(String sessionId, Duration ttl) {
    redisTemplate.expire(key(sessionId), ttl);
  }

  @Override
  public void delete(String sessionId) {
    redisTemplate.delete(key(sessionId));
  }

  private String serialize(Session session) {
    try {
      return objectMapper.writeValueAsString(session);
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException("Unable to serialize active session " + session.getSessionId(), ex);
    }
  }

  private String key(String sessionId) {
    return "session:active:" + sessionId;
  }
}
