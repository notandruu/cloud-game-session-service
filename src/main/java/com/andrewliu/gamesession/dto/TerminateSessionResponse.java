package com.andrewliu.gamesession.dto;

import com.andrewliu.gamesession.model.Session;
import com.andrewliu.gamesession.model.SessionStatus;

public record TerminateSessionResponse(
    String sessionId,
    SessionStatus status,
    String releasedNodeId) {

  public static TerminateSessionResponse from(Session session, String releasedNodeId) {
    return new TerminateSessionResponse(session.getSessionId(), session.getStatus(), releasedNodeId);
  }
}
