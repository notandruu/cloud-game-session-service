package com.andrewliu.gamesession.dto;

public record MetricsResponse(
    long activeSessions,
    long queuedSessions,
    long registeredNodes,
    long totalCapacity,
    long usedCapacity,
    long availableCapacity,
    long allocationFailures,
    long expiredSessions,
    double averageQueueWaitMillis) {
}
