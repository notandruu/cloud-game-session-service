package com.andrewliu.gamesession.model;

import java.time.Instant;
import java.util.Objects;

public class Session {

  private String sessionId;
  private String userId;
  private String gameId;
  private String region;
  private SessionStatus status;
  private String nodeId;
  private Instant createdAt;
  private Instant updatedAt;
  private Instant lastHeartbeatAt;
  private Instant queueEnteredAt;
  private Instant terminatedAt;

  public Session() {
  }

  public Session(
      String sessionId,
      String userId,
      String gameId,
      String region,
      SessionStatus status,
      String nodeId,
      Instant createdAt,
      Instant updatedAt,
      Instant lastHeartbeatAt,
      Instant queueEnteredAt,
      Instant terminatedAt) {
    this.sessionId = sessionId;
    this.userId = userId;
    this.gameId = gameId;
    this.region = region;
    this.status = status;
    this.nodeId = nodeId;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.lastHeartbeatAt = lastHeartbeatAt;
    this.queueEnteredAt = queueEnteredAt;
    this.terminatedAt = terminatedAt;
  }

  public static Session allocating(
      String sessionId, String userId, String gameId, String region, Instant now) {
    return new Session(
        sessionId,
        userId,
        gameId,
        region,
        SessionStatus.ALLOCATING,
        null,
        now,
        now,
        null,
        null,
        null);
  }

  public Session copy() {
    return new Session(
        sessionId,
        userId,
        gameId,
        region,
        status,
        nodeId,
        createdAt,
        updatedAt,
        lastHeartbeatAt,
        queueEnteredAt,
        terminatedAt);
  }

  public void transitionTo(SessionStatus next, Instant now) {
    if (!status.canTransitionTo(next)) {
      throw new IllegalStateException("Invalid session transition from " + status + " to " + next);
    }
    status = next;
    updatedAt = now;
  }

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getGameId() {
    return gameId;
  }

  public void setGameId(String gameId) {
    this.gameId = gameId;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public SessionStatus getStatus() {
    return status;
  }

  public void setStatus(SessionStatus status) {
    this.status = status;
  }

  public String getNodeId() {
    return nodeId;
  }

  public void setNodeId(String nodeId) {
    this.nodeId = nodeId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  public Instant getLastHeartbeatAt() {
    return lastHeartbeatAt;
  }

  public void setLastHeartbeatAt(Instant lastHeartbeatAt) {
    this.lastHeartbeatAt = lastHeartbeatAt;
  }

  public Instant getQueueEnteredAt() {
    return queueEnteredAt;
  }

  public void setQueueEnteredAt(Instant queueEnteredAt) {
    this.queueEnteredAt = queueEnteredAt;
  }

  public Instant getTerminatedAt() {
    return terminatedAt;
  }

  public void setTerminatedAt(Instant terminatedAt) {
    this.terminatedAt = terminatedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Session session)) {
      return false;
    }
    return Objects.equals(sessionId, session.sessionId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sessionId);
  }
}
