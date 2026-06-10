package com.andrewliu.gamesession.repository;

import com.andrewliu.gamesession.model.Session;
import com.andrewliu.gamesession.model.SessionStatus;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "firestore")
public class FirestoreSessionRepository implements SessionRepository {

  private static final String COLLECTION = "sessions";

  private final Firestore firestore;

  public FirestoreSessionRepository(Firestore firestore) {
    this.firestore = firestore;
  }

  @Override
  public Session save(Session session) {
    await(firestore.collection(COLLECTION).document(session.getSessionId()).set(toMap(session)));
    return session.copy();
  }

  @Override
  public Optional<Session> findById(String sessionId) {
    DocumentSnapshot snapshot =
        await(firestore.collection(COLLECTION).document(sessionId).get());
    if (!snapshot.exists()) {
      return Optional.empty();
    }
    return Optional.of(fromDocument(snapshot));
  }

  @Override
  public List<Session> findByStatus(SessionStatus status) {
    return await(firestore.collection(COLLECTION)
            .whereEqualTo("status", status.name())
            .get())
        .getDocuments()
        .stream()
        .map(this::fromDocument)
        .sorted(Comparator.comparing(Session::getCreatedAt))
        .toList();
  }

  @Override
  public List<Session> findByRegionAndStatus(String region, SessionStatus status) {
    return await(firestore.collection(COLLECTION)
            .whereEqualTo("region", region)
            .whereEqualTo("status", status.name())
            .get())
        .getDocuments()
        .stream()
        .map(this::fromDocument)
        .sorted(Comparator.comparing(Session::getCreatedAt))
        .toList();
  }

  @Override
  public List<Session> findAll() {
    return await(firestore.collection(COLLECTION).get())
        .getDocuments()
        .stream()
        .map(this::fromDocument)
        .sorted(Comparator.comparing(Session::getCreatedAt))
        .toList();
  }

  @Override
  public long countByStatus(SessionStatus status) {
    return findByStatus(status).size();
  }

  private Map<String, Object> toMap(Session session) {
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("sessionId", session.getSessionId());
    data.put("userId", session.getUserId());
    data.put("gameId", session.getGameId());
    data.put("region", session.getRegion());
    data.put("status", session.getStatus().name());
    data.put("nodeId", session.getNodeId());
    data.put("createdAt", date(session.getCreatedAt()));
    data.put("updatedAt", date(session.getUpdatedAt()));
    data.put("lastHeartbeatAt", date(session.getLastHeartbeatAt()));
    data.put("queueEnteredAt", date(session.getQueueEnteredAt()));
    data.put("terminatedAt", date(session.getTerminatedAt()));
    return data;
  }

  private Session fromDocument(DocumentSnapshot document) {
    return new Session(
        document.getString("sessionId"),
        document.getString("userId"),
        document.getString("gameId"),
        document.getString("region"),
        SessionStatus.valueOf(document.getString("status")),
        document.getString("nodeId"),
        instant(document.getDate("createdAt")),
        instant(document.getDate("updatedAt")),
        instant(document.getDate("lastHeartbeatAt")),
        instant(document.getDate("queueEnteredAt")),
        instant(document.getDate("terminatedAt")));
  }

  private Session fromDocument(QueryDocumentSnapshot document) {
    return fromDocument((DocumentSnapshot) document);
  }

  private Date date(Instant instant) {
    return instant == null ? null : Date.from(instant);
  }

  private Instant instant(Date date) {
    return date == null ? null : date.toInstant();
  }

  private <T> T await(com.google.api.core.ApiFuture<T> future) {
    try {
      return future.get();
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Firestore operation interrupted", ex);
    } catch (ExecutionException ex) {
      throw new IllegalStateException("Firestore operation failed", ex);
    }
  }
}
