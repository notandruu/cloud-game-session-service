package com.andrewliu.gamesession.repository;

import com.andrewliu.gamesession.model.GpuNode;
import java.util.List;
import java.util.Optional;

public interface NodeRepository {

  GpuNode save(GpuNode node);

  Optional<GpuNode> findById(String nodeId);

  List<GpuNode> findByRegion(String region);

  List<GpuNode> findAll();
}
