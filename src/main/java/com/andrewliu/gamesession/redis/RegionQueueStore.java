package com.andrewliu.gamesession.redis;

import java.util.Optional;

public interface RegionQueueStore {

  void enqueue(String region, String sessionId);

  void requeueFront(String region, String sessionId);

  Optional<String> pop(String region);

  void remove(String region, String sessionId);

  int position(String region, String sessionId);

  long size(String region);
}
