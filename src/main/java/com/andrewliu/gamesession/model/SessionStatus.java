package com.andrewliu.gamesession.model;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum SessionStatus {
  QUEUED,
  ALLOCATING,
  ACTIVE,
  EXPIRED,
  TERMINATED,
  FAILED;

  private static final Map<SessionStatus, Set<SessionStatus>> VALID_TRANSITIONS =
      new EnumMap<>(SessionStatus.class);

  static {
    VALID_TRANSITIONS.put(QUEUED, EnumSet.of(ALLOCATING, TERMINATED));
    VALID_TRANSITIONS.put(ALLOCATING, EnumSet.of(ACTIVE, FAILED, QUEUED, TERMINATED));
    VALID_TRANSITIONS.put(ACTIVE, EnumSet.of(TERMINATED, EXPIRED));
    VALID_TRANSITIONS.put(FAILED, EnumSet.of(QUEUED));
    VALID_TRANSITIONS.put(EXPIRED, EnumSet.noneOf(SessionStatus.class));
    VALID_TRANSITIONS.put(TERMINATED, EnumSet.noneOf(SessionStatus.class));
  }

  public boolean canTransitionTo(SessionStatus next) {
    return this == next || VALID_TRANSITIONS.getOrDefault(this, Set.of()).contains(next);
  }

  public boolean isTerminal() {
    return this == EXPIRED || this == TERMINATED;
  }
}
