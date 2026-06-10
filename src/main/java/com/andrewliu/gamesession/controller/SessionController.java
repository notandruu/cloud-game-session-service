package com.andrewliu.gamesession.controller;

import com.andrewliu.gamesession.dto.CreateSessionRequest;
import com.andrewliu.gamesession.dto.HeartbeatResponse;
import com.andrewliu.gamesession.dto.SessionResponse;
import com.andrewliu.gamesession.dto.TerminateSessionResponse;
import com.andrewliu.gamesession.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sessions")
public class SessionController {

  private final SessionService sessionService;

  public SessionController(SessionService sessionService) {
    this.sessionService = sessionService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public SessionResponse create(@Valid @RequestBody CreateSessionRequest request) {
    return sessionService.create(request);
  }

  @GetMapping("/{sessionId}")
  public SessionResponse get(@PathVariable String sessionId) {
    return sessionService.get(sessionId);
  }

  @PostMapping("/{sessionId}/heartbeat")
  public HeartbeatResponse heartbeat(@PathVariable String sessionId) {
    return sessionService.heartbeat(sessionId);
  }

  @DeleteMapping("/{sessionId}")
  public TerminateSessionResponse terminate(@PathVariable String sessionId) {
    return sessionService.terminate(sessionId);
  }
}
