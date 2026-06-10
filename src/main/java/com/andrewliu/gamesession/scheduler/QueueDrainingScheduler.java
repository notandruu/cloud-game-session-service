package com.andrewliu.gamesession.scheduler;

import com.andrewliu.gamesession.service.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class QueueDrainingScheduler {

  private static final Logger log = LoggerFactory.getLogger(QueueDrainingScheduler.class);

  private final QueueService queueService;

  public QueueDrainingScheduler(QueueService queueService) {
    this.queueService = queueService;
  }

  @Scheduled(fixedDelayString = "#{@appProperties.scheduler.queueDrainDelay.toMillis()}")
  public void drainQueues() {
    int allocated = queueService.drainAllRegions();
    if (allocated > 0) {
      log.info("queue_drain_completed allocated={}", allocated);
    }
  }
}
