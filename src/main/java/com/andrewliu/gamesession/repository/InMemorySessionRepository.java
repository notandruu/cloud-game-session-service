package com.andrewliu.gamesession.repository;

import com.andrewliu.gamesession.model.Session;
import com.andrewliu.gamesession.model.SessionStatus;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "memory", matchIfMissing = true)
public class InMemorySessionRepository implements SessionRepository {

  private final ConcurrentMap<String, Session> sessions = new ConcurrentHashMap<>();

  @Override
  public Session save(Session session) {
    sessions.put(session.getSessionId(), session.copy());
    return session.copy();
  }

  @Override
  public Optional<Session> findById(String sessionId) {
    return Optional.ofNullable(sessions.get(sessionId)).map(Session::copy);
  }

  @Override
  public List<Session> findByStatus(SessionStatus status) {
    return sessions.values().stream()
        .filter(session -> session.getStatus() == status)
        .map(Session::copy)
        .sorted(Comparator.comparing(Session::getCreatedAt))
        .toList();
  }

  @Override
  public List<Session> findByRegionAndStatus(String region, SessionStatus status) {
    return sessions.values().stream()
        .filter(session -> session.getRegion().equals(region))
        .filter(session -> session.getStatus() == status)
        .map(Session::copy)
        .sorted(Comparator.comparing(Session::getCreatedAt))
        .toList();
  }

  @Override
  public List<Session> findAll() {
    return sessions.values().stream()
        .map(Session::copy)
        .sorted(Comparator.comparing(Session::getCreatedAt))
        .toList();
  }

  @Override
  public long countByStatus(SessionStatus status) {
    return sessions.values().stream().filter(session -> session.getStatus() == status).count();
  }
}
