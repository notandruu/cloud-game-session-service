package com.andrewliu.gamesession.service;

import com.andrewliu.gamesession.config.AppProperties;
import com.andrewliu.gamesession.dto.CreateSessionRequest;
import com.andrewliu.gamesession.dto.HeartbeatResponse;
import com.andrewliu.gamesession.dto.SessionResponse;
import com.andrewliu.gamesession.dto.TerminateSessionResponse;
import com.andrewliu.gamesession.exception.BadRequestException;
import com.andrewliu.gamesession.exception.ConflictException;
import com.andrewliu.gamesession.exception.NotFoundException;
import com.andrewliu.gamesession.model.Session;
import com.andrewliu.gamesession.model.SessionStatus;
import com.andrewliu.gamesession.redis.ActiveSessionStore;
import com.andrewliu.gamesession.repository.SessionRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

  private static final Logger log = LoggerFactory.getLogger(SessionService.class);
  private static final Pattern SESSION_ID_PATTERN = Pattern.compile("sess_[A-Za-z0-9_-]{8,80}");

  private final SessionRepository sessionRepository;
  private final ActiveSessionStore activeSessionStore;
  private final AllocationService allocationService;
  private final QueueService queueService;
  private final AppProperties properties;
  private final OperationalMetrics operationalMetrics;
  private final Clock clock;

  public SessionService(
      SessionRepository sessionRepository,
      ActiveSessionStore activeSessionStore,
      AllocationService allocationService,
      QueueService queueService,
      AppProperties properties,
      OperationalMetrics operationalMetrics,
      Clock clock) {
    this.sessionRepository = sessionRepository;
    this.activeSessionStore = activeSessionStore;
    this.allocationService = allocationService;
    this.queueService = queueService;
    this.properties = properties;
    this.operationalMetrics = operationalMetrics;
    this.clock = clock;
  }

  public synchronized SessionResponse create(CreateSessionRequest request) {
    Instant now = clock.instant();
    Session session = Session.allocating(
        nextSessionId(), request.userId(), request.gameId(), request.region(), now);
    sessionRepository.save(session);

    Session saved = allocationService.tryActivate(session)
        .orElseGet(() -> queue(session, now));

    log.info(
        "session_created sessionId={} userId={} gameId={} region={} status={}",
        saved.getSessionId(),
        saved.getUserId(),
        saved.getGameId(),
        saved.getRegion(),
        saved.getStatus());
    return response(saved);
  }

  public SessionResponse get(String sessionId) {
    validateSessionId(sessionId);
    return response(findRequired(sessionId));
  }

  public synchronized HeartbeatResponse heartbeat(String sessionId) {
    validateSessionId(sessionId);
    Session session = findRequired(sessionId);
    if (session.getStatus() != SessionStatus.ACTIVE) {
      throw new ConflictException("Heartbeat is only accepted for ACTIVE sessions");
    }
    Instant now = clock.instant();
    session.setLastHeartbeatAt(now);
    session.setUpdatedAt(now);
    Session saved = sessionRepository.save(session);
    activeSessionStore.save(saved, properties.getActiveSessionTtl());
    log.info("session_heartbeat sessionId={} nodeId={}", saved.getSessionId(), saved.getNodeId());
    return HeartbeatResponse.from(saved);
  }

  public synchronized TerminateSessionResponse terminate(String sessionId) {
    validateSessionId(sessionId);
    Session session = findRequired(sessionId);
    if (session.getStatus() == SessionStatus.TERMINATED) {
      return TerminateSessionResponse.from(session, null);
    }
    if (session.getStatus() == SessionStatus.EXPIRED) {
      throw new ConflictException("Expired sessions cannot be terminated");
    }

    String releasedNodeId = null;
    if (session.getStatus() == SessionStatus.QUEUED) {
      queueService.remove(session);
    }
    if (session.getStatus() == SessionStatus.ACTIVE) {
      releasedNodeId = allocationService.release(session);
    }

    Instant now = clock.instant();
    session.transitionTo(SessionStatus.TERMINATED, now);
    session.setTerminatedAt(now);
    sessionRepository.save(session);
    log.info("session_terminated sessionId={} releasedNodeId={}", session.getSessionId(), releasedNodeId);

    if (releasedNodeId != null) {
      queueService.drainRegion(session.getRegion());
    }
    return TerminateSessionResponse.from(session, releasedNodeId);
  }

  public synchronized int expireStaleSessions() {
    Instant deadline = clock.instant().minus(properties.getHeartbeatTimeout());
    Set<String> releasedRegions = new LinkedHashSet<>();
    int expired = 0;

    for (Session session : sessionRepository.findByStatus(SessionStatus.ACTIVE)) {
      Instant lastHeartbeat = session.getLastHeartbeatAt();
      if (lastHeartbeat != null && lastHeartbeat.isAfter(deadline)) {
        continue;
      }

      allocationService.release(session);
      session.transitionTo(SessionStatus.EXPIRED, clock.instant());
      session.setTerminatedAt(clock.instant());
      sessionRepository.save(session);
      operationalMetrics.recordExpiredSession();
      releasedRegions.add(session.getRegion());
      expired++;
      log.info("session_expired sessionId={} nodeId={}", session.getSessionId(), session.getNodeId());
    }

    releasedRegions.forEach(queueService::drainRegion);
    return expired;
  }

  private Session queue(Session session, Instant now) {
    session.transitionTo(SessionStatus.QUEUED, now);
    session.setQueueEnteredAt(now);
    session.setNodeId(null);
    Session saved = sessionRepository.save(session);
    queueService.enqueue(saved);
    return saved;
  }

  private SessionResponse response(Session session) {
    Integer queuePosition = null;
    if (session.getStatus() == SessionStatus.QUEUED) {
      int position = queueService.position(session);
      queuePosition = position < 0 ? null : position;
    }
    return SessionResponse.from(session, queuePosition);
  }

  private Session findRequired(String sessionId) {
    return sessionRepository.findById(sessionId)
        .orElseThrow(() -> new NotFoundException("Session not found: " + sessionId));
  }

  private void validateSessionId(String sessionId) {
    if (sessionId == null || !SESSION_ID_PATTERN.matcher(sessionId).matches()) {
      throw new BadRequestException("Invalid session ID");
    }
  }

  private String nextSessionId() {
    return "sess_" + UUID.randomUUID().toString().replace("-", "");
  }
}
