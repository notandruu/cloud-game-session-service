package com.andrewliu.gamesession.repository;

import com.andrewliu.gamesession.model.GpuNode;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "memory", matchIfMissing = true)
public class InMemoryNodeRepository implements NodeRepository {

  private final ConcurrentMap<String, GpuNode> nodes = new ConcurrentHashMap<>();

  @Override
  public GpuNode save(GpuNode node) {
    nodes.put(node.getNodeId(), copy(node));
    return copy(node);
  }

  @Override
  public Optional<GpuNode> findById(String nodeId) {
    return Optional.ofNullable(nodes.get(nodeId)).map(this::copy);
  }

  @Override
  public List<GpuNode> findByRegion(String region) {
    return nodes.values().stream()
        .filter(node -> node.getRegion().equals(region))
        .map(this::copy)
        .sorted(Comparator.comparing(GpuNode::getNodeId))
        .toList();
  }

  @Override
  public List<GpuNode> findAll() {
    return nodes.values().stream()
        .map(this::copy)
        .sorted(Comparator.comparing(GpuNode::getNodeId))
        .toList();
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
}
