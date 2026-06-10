package com.andrewliu.gamesession.dto;

import com.andrewliu.gamesession.model.Session;
import com.andrewliu.gamesession.model.SessionStatus;
import java.time.Instant;

public record HeartbeatResponse(
    String sessionId,
    SessionStatus status,
    Instant lastHeartbeatAt) {

  public static HeartbeatResponse from(Session session) {
    return new HeartbeatResponse(
        session.getSessionId(), session.getStatus(), session.getLastHeartbeatAt());
  }
}
