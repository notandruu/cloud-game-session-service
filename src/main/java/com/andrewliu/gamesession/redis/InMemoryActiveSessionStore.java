package com.andrewliu.gamesession.redis;

import com.andrewliu.gamesession.model.Session;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.store.type", havingValue = "memory", matchIfMissing = true)
public class InMemoryActiveSessionStore implements ActiveSessionStore {

  private final ConcurrentMap<String, CachedSession> sessions = new ConcurrentHashMap<>();

  @Override
  public void save(Session session, Duration ttl) {
    sessions.put(session.getSessionId(), new CachedSession(session.copy(), Instant.now().plus(ttl)));
  }

  @Override
  public Optional<Session> find(String sessionId) {
    CachedSession cached = sessions.get(sessionId);
    if (cached == null) {
      return Optional.empty();
    }
    if (cached.expiresAt().isBefore(Instant.now())) {
      sessions.remove(sessionId);
      return Optional.empty();
    }
    return Optional.of(cached.session().copy());
  }

  @Override
  public void refreshTtl(String sessionId, Duration ttl) {
    sessions.computeIfPresent(
        sessionId, (ignored, cached) -> new CachedSession(cached.session(), Instant.now().plus(ttl)));
  }

  @Override
  public void delete(String sessionId) {
    sessions.remove(sessionId);
  }

  private record CachedSession(Session session, Instant expiresAt) {
  }
}
