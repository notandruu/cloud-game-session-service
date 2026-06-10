package com.andrewliu.gamesession.scheduler;

import com.andrewliu.gamesession.service.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SessionExpirationScheduler {

  private static final Logger log = LoggerFactory.getLogger(SessionExpirationScheduler.class);

  private final SessionService sessionService;

  public SessionExpirationScheduler(SessionService sessionService) {
    this.sessionService = sessionService;
  }

  @Scheduled(fixedDelayString = "#{@appProperties.scheduler.expirationDelay.toMillis()}")
  public void expireStaleSessions() {
    int expired = sessionService.expireStaleSessions();
    if (expired > 0) {
      log.info("session_expiration_completed expired={}", expired);
    }
  }
}
