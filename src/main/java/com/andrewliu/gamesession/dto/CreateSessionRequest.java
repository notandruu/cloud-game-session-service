package com.andrewliu.gamesession.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateSessionRequest(
    @NotBlank String userId,
    @NotBlank String gameId,
    @NotBlank String region) {
}
