package com.andrewliu.gamesession.service;

import com.andrewliu.gamesession.dto.RegisterNodeRequest;
import com.andrewliu.gamesession.exception.NotFoundException;
import com.andrewliu.gamesession.model.GpuNode;
import com.andrewliu.gamesession.redis.NodeCapacityStore;
import com.andrewliu.gamesession.repository.NodeRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NodeService {

  private static final Logger log = LoggerFactory.getLogger(NodeService.class);

  private final NodeRepository nodeRepository;
  private final NodeCapacityStore capacityStore;
  private final Clock clock;

  public NodeService(NodeRepository nodeRepository, NodeCapacityStore capacityStore, Clock clock) {
    this.nodeRepository = nodeRepository;
    this.capacityStore = capacityStore;
    this.clock = clock;
  }

  public synchronized GpuNode register(RegisterNodeRequest request) {
    Instant now = clock.instant();
    GpuNode node = nodeRepository.findById(request.nodeId())
        .orElseGet(() -> GpuNode.create(request.nodeId(), request.region(), request.maxSessions(), now));
    node.setRegion(request.region());
    node.setMaxSessions(request.maxSessions());
    node.setActiveSessions(Math.min(capacityStore.getActiveSessions(node.getNodeId()), request.maxSessions()));
    node.setUpdatedAt(now);
    node.refreshStatus();
    capacityStore.setActiveSessions(node.getNodeId(), node.getActiveSessions());
    GpuNode saved = nodeRepository.save(node);
    log.info(
        "gpu_node_registered nodeId={} region={} maxSessions={}",
        saved.getNodeId(),
        saved.getRegion(),
        saved.getMaxSessions());
    return saved;
  }

  public GpuNode getRequired(String nodeId) {
    return nodeRepository.findById(nodeId)
        .orElseThrow(() -> new NotFoundException("GPU node not found: " + nodeId));
  }

  public List<GpuNode> list() {
    return nodeRepository.findAll().stream().map(this::withLiveCapacity).toList();
  }

  public List<GpuNode> listByRegion(String region) {
    return nodeRepository.findByRegion(region).stream().map(this::withLiveCapacity).toList();
  }

  public GpuNode save(GpuNode node) {
    node.refreshStatus();
    return nodeRepository.save(node);
  }

  private GpuNode withLiveCapacity(GpuNode node) {
    node.setActiveSessions(capacityStore.getActiveSessions(node.getNodeId()));
    node.refreshStatus();
    return node;
  }
}
