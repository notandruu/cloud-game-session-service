package com.andrewliu.gamesession.model;

import java.time.Instant;
import java.util.Objects;

public class GpuNode {

  private String nodeId;
  private String region;
  private int maxSessions;
  private int activeSessions;
  private NodeStatus status;
  private Instant createdAt;
  private Instant updatedAt;

  public GpuNode() {
  }

  public GpuNode(
      String nodeId,
      String region,
      int maxSessions,
      int activeSessions,
      NodeStatus status,
      Instant createdAt,
      Instant updatedAt) {
    this.nodeId = nodeId;
    this.region = region;
    this.maxSessions = maxSessions;
    this.activeSessions = activeSessions;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static GpuNode create(String nodeId, String region, int maxSessions, Instant now) {
    return new GpuNode(nodeId, region, maxSessions, 0, NodeStatus.AVAILABLE, now, now);
  }

  public void refreshStatus() {
    if (status == NodeStatus.OFFLINE) {
      return;
    }
    status = activeSessions >= maxSessions ? NodeStatus.FULL : NodeStatus.AVAILABLE;
  }

  public String getNodeId() {
    return nodeId;
  }

  public void setNodeId(String nodeId) {
    this.nodeId = nodeId;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public int getMaxSessions() {
    return maxSessions;
  }

  public void setMaxSessions(int maxSessions) {
    this.maxSessions = maxSessions;
  }

  public int getActiveSessions() {
    return activeSessions;
  }

  public void setActiveSessions(int activeSessions) {
    this.activeSessions = activeSessions;
  }

  public NodeStatus getStatus() {
    return status;
  }

  public void setStatus(NodeStatus status) {
    this.status = status;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof GpuNode gpuNode)) {
      return false;
    }
    return Objects.equals(nodeId, gpuNode.nodeId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nodeId);
  }
}
