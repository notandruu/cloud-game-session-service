package com.andrewliu.gamesession.controller;

import com.andrewliu.gamesession.config.AppProperties;
import com.andrewliu.gamesession.dto.MetricsResponse;
import com.andrewliu.gamesession.exception.UnauthorizedException;
import com.andrewliu.gamesession.service.MetricsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

  private final MetricsService metricsService;
  private final AppProperties properties;

  public AdminController(MetricsService metricsService, AppProperties properties) {
    this.metricsService = metricsService;
    this.properties = properties;
  }

  @GetMapping("/metrics")
  public MetricsResponse metrics(@RequestHeader(name = "X-Internal-Api-Key", required = false) String apiKey) {
    if (!properties.getAdminApiKey().equals(apiKey)) {
      throw new UnauthorizedException("Missing or invalid internal API key");
    }
    return metricsService.metrics();
  }
}
