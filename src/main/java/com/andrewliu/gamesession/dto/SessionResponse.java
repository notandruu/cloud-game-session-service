package com.andrewliu.gamesession.dto;

import com.andrewliu.gamesession.model.Session;
import com.andrewliu.gamesession.model.SessionStatus;
import java.time.Instant;

public record SessionResponse(
    String sessionId,
    String userId,
    String gameId,
    String region,
    SessionStatus status,
    String nodeId,
    Integer queuePosition,
    Instant createdAt,
    Instant updatedAt,
    Instant lastHeartbeatAt) {

  public static SessionResponse from(Session session, Integer queuePosition) {
    return new SessionResponse(
        session.getSessionId(),
        session.getUserId(),
        session.getGameId(),
        session.getRegion(),
        session.getStatus(),
        session.getNodeId(),
        queuePosition,
        session.getCreatedAt(),
        session.getUpdatedAt(),
        session.getLastHeartbeatAt());
  }
}
