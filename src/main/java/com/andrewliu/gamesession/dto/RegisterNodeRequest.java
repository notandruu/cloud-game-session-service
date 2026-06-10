package com.andrewliu.gamesession.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record RegisterNodeRequest(
    @NotBlank String nodeId,
    @NotBlank String region,
    @Min(1) int maxSessions) {
}
