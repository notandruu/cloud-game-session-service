package com.andrewliu.gamesession.controller;

import com.andrewliu.gamesession.dto.NodeResponse;
import com.andrewliu.gamesession.dto.NodesResponse;
import com.andrewliu.gamesession.dto.RegisterNodeRequest;
import com.andrewliu.gamesession.service.NodeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/nodes")
public class NodeController {

  private final NodeService nodeService;

  public NodeController(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public NodeResponse register(@Valid @RequestBody RegisterNodeRequest request) {
    return NodeResponse.from(nodeService.register(request));
  }

  @GetMapping
  public NodesResponse list() {
    return new NodesResponse(nodeService.list().stream().map(NodeResponse::from).toList());
  }
}
