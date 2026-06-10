package com.andrewliu.gamesession.service;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class OperationalMetrics {

  private final AtomicLong allocationFailures = new AtomicLong();
  private final AtomicLong expiredSessions = new AtomicLong();
  private final AtomicLong queueWaitMillisTotal = new AtomicLong();
  private final AtomicLong queueWaitSamples = new AtomicLong();

  public void recordAllocationFailure() {
    allocationFailures.incrementAndGet();
  }

  public void recordExpiredSession() {
    expiredSessions.incrementAndGet();
  }

  public void recordQueueWait(Duration duration) {
    queueWaitMillisTotal.addAndGet(Math.max(0, duration.toMillis()));
    queueWaitSamples.incrementAndGet();
  }

  public long allocationFailures() {
    return allocationFailures.get();
  }

  public long expiredSessions() {
    return expiredSessions.get();
  }

  public double averageQueueWaitMillis() {
    long samples = queueWaitSamples.get();
    if (samples == 0) {
      return 0.0;
    }
    return (double) queueWaitMillisTotal.get() / samples;
  }
}
