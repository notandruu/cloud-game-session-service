package com.andrewliu.gamesession.repository;

import com.andrewliu.gamesession.model.GpuNode;
import com.andrewliu.gamesession.model.NodeStatus;
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
public class FirestoreNodeRepository implements NodeRepository {

  private static final String COLLECTION = "nodes";

  private final Firestore firestore;

  public FirestoreNodeRepository(Firestore firestore) {
    this.firestore = firestore;
  }

  @Override
  public GpuNode save(GpuNode node) {
    await(firestore.collection(COLLECTION).document(node.getNodeId()).set(toMap(node)));
    return copy(node);
  }

  @Override
  public Optional<GpuNode> findById(String nodeId) {
    DocumentSnapshot snapshot = await(firestore.collection(COLLECTION).document(nodeId).get());
    if (!snapshot.exists()) {
      return Optional.empty();
    }
    return Optional.of(fromDocument(snapshot));
  }

  @Override
  public List<GpuNode> findByRegion(String region) {
    return await(firestore.collection(COLLECTION).whereEqualTo("region", region).get())
        .getDocuments()
        .stream()
        .map(this::fromDocument)
        .sorted(Comparator.comparing(GpuNode::getNodeId))
        .toList();
  }

  @Override
  public List<GpuNode> findAll() {
    return await(firestore.collection(COLLECTION).get())
        .getDocuments()
        .stream()
        .map(this::fromDocument)
        .sorted(Comparator.comparing(GpuNode::getNodeId))
        .toList();
  }

  private Map<String, Object> toMap(GpuNode node) {
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("nodeId", node.getNodeId());
    data.put("region", node.getRegion());
    data.put("maxSessions", node.getMaxSessions());
    data.put("activeSessions", node.getActiveSessions());
    data.put("status", node.getStatus().name());
    data.put("createdAt", date(node.getCreatedAt()));
    data.put("updatedAt", date(node.getUpdatedAt()));
    return data;
  }

  private GpuNode fromDocument(QueryDocumentSnapshot document) {
    return fromDocument((DocumentSnapshot) document);
  }

  private GpuNode fromDocument(DocumentSnapshot document) {
    return new GpuNode(
        document.getString("nodeId"),
        document.getString("region"),
        intValue(document.getLong("maxSessions")),
        intValue(document.getLong("activeSessions")),
        NodeStatus.valueOf(document.getString("status")),
        instant(document.getDate("createdAt")),
        instant(document.getDate("updatedAt")));
  }

  private GpuNode copy(GpuNode node) {
    return new GpuNode(
        node.getNodeId(),
        node.getRegion(),
        node.getMaxSessions(),
        node.getActiveSessions(),
        node.getStatus(),
        node.getCreatedAt(),
        node.getUpdatedAt());
  }

  private int intValue(Long value) {
    return value == null ? 0 : value.intValue();
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
