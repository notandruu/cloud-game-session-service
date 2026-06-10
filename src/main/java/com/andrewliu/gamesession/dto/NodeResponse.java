package com.andrewliu.gamesession.dto;

import com.andrewliu.gamesession.model.GpuNode;
import com.andrewliu.gamesession.model.NodeStatus;
import java.time.Instant;

public record NodeResponse(
    String nodeId,
    String region,
    int maxSessions,
    int activeSessions,
    NodeStatus status,
    Instant createdAt,
    Instant updatedAt) {

  public static NodeResponse from(GpuNode node) {
    return new NodeResponse(
        node.getNodeId(),
        node.getRegion(),
        node.getMaxSessions(),
        node.getActiveSessions(),
        node.getStatus(),
        node.getCreatedAt(),
        node.getUpdatedAt());
  }
}
