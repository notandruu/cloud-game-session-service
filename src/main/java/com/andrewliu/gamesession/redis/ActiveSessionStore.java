package com.andrewliu.gamesession.redis;

import com.andrewliu.gamesession.model.Session;
import java.time.Duration;
import java.util.Optional;

public interface ActiveSessionStore {

  void save(Session session, Duration ttl);

  Optional<Session> find(String sessionId);

  void refreshTtl(String sessionId, Duration ttl);

  void delete(String sessionId);
}
