package com.andrewliu.gamesession.repository;

import com.andrewliu.gamesession.model.Session;
import com.andrewliu.gamesession.model.SessionStatus;
import java.util.List;
import java.util.Optional;

public interface SessionRepository {

  Session save(Session session);

  Optional<Session> findById(String sessionId);

  List<Session> findByStatus(SessionStatus status);

  List<Session> findByRegionAndStatus(String region, SessionStatus status);

  List<Session> findAll();

  long countByStatus(SessionStatus status);
}
