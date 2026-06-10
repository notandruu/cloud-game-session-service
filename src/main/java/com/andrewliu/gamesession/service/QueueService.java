package com.andrewliu.gamesession.service;

import com.andrewliu.gamesession.model.Session;
import com.andrewliu.gamesession.model.SessionStatus;
import com.andrewliu.gamesession.redis.RegionQueueStore;
import com.andrewliu.gamesession.repository.NodeRepository;
import com.andrewliu.gamesession.repository.SessionRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class QueueService {

  private static final Logger log = LoggerFactory.getLogger(QueueService.class);

  private final RegionQueueStore queueStore;
  private final SessionRepository sessionRepository;
  private final NodeRepository nodeRepository;
  private final AllocationService allocationService;
  private final Clock clock;

  public QueueService(
      RegionQueueStore queueStore,
      SessionRepository sessionRepository,
      NodeRepository nodeRepository,
      AllocationService allocationService,
      Clock clock) {
    this.queueStore = queueStore;
    this.sessionRepository = sessionRepository;
    this.nodeRepository = nodeRepository;
    this.allocationService = allocationService;
    this.clock = clock;
  }

  public void enqueue(Session session) {
    queueStore.enqueue(session.getRegion(), session.getSessionId());
    log.info(
        "session_queued sessionId={} userId={} gameId={} region={} position={}",
        session.getSessionId(),
        session.getUserId(),
        session.getGameId(),
        session.getRegion(),
        position(session));
  }

  public void remove(Session session) {
    queueStore.remove(session.getRegion(), session.getSessionId());
  }

  public int position(Session session) {
    return queueStore.position(session.getRegion(), session.getSessionId());
  }

  public synchronized int drainAllRegions() {
    int allocated = 0;
    for (String region : regionsWithWork()) {
      allocated += drainRegion(region);
    }
    return allocated;
  }

  public synchronized int drainRegion(String region) {
    int allocated = 0;
    while (true) {
      Optional<String> nextSessionId = queueStore.pop(region);
      if (nextSessionId.isEmpty()) {
        return allocated;
      }

      Optional<Session> maybeSession = sessionRepository.findById(nextSessionId.get());
      if (maybeSession.isEmpty()) {
        continue;
      }

      Session session = maybeSession.get();
      if (session.getStatus() != SessionStatus.QUEUED) {
        continue;
      }

      Instant now = clock.instant();
      session.transitionTo(SessionStatus.ALLOCATING, now);
      sessionRepository.save(session);

      Optional<Session> activeSession = allocationService.tryActivate(session);
      if (activeSession.isPresent()) {
        allocated++;
        continue;
      }

      session.transitionTo(SessionStatus.QUEUED, clock.instant());
      sessionRepository.save(session);
      queueStore.requeueFront(region, session.getSessionId());
      return allocated;
    }
  }

  public long queuedCountByRegion(String region) {
    return queueStore.size(region);
  }

  private Set<String> regionsWithWork() {
    Set<String> regions = new LinkedHashSet<>();
    nodeRepository.findAll().forEach(node -> regions.add(node.getRegion()));
    sessionRepository.findByStatus(SessionStatus.QUEUED).forEach(session -> regions.add(session.getRegion()));
    return regions;
  }
}
