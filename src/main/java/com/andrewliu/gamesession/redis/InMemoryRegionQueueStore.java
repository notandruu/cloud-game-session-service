package com.andrewliu.gamesession.redis;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.store.type", havingValue = "memory", matchIfMissing = true)
public class InMemoryRegionQueueStore implements RegionQueueStore {

  private final ConcurrentMap<String, Deque<String>> queues = new ConcurrentHashMap<>();

  @Override
  public synchronized void enqueue(String region, String sessionId) {
    Deque<String> queue = queue(region);
    if (!queue.contains(sessionId)) {
      queue.addLast(sessionId);
    }
  }

  @Override
  public synchronized void requeueFront(String region, String sessionId) {
    Deque<String> queue = queue(region);
    queue.remove(sessionId);
    queue.addFirst(sessionId);
  }

  @Override
  public synchronized Optional<String> pop(String region) {
    return Optional.ofNullable(queue(region).pollFirst());
  }

  @Override
  public synchronized void remove(String region, String sessionId) {
    queue(region).remove(sessionId);
  }

  @Override
  public synchronized int position(String region, String sessionId) {
    int index = 1;
    Iterator<String> iterator = queue(region).iterator();
    while (iterator.hasNext()) {
      if (iterator.next().equals(sessionId)) {
        return index;
      }
      index++;
    }
    return -1;
  }

  @Override
  public synchronized long size(String region) {
    return queue(region).size();
  }

  private Deque<String> queue(String region) {
    return queues.computeIfAbsent(region, ignored -> new ArrayDeque<>());
  }
}
