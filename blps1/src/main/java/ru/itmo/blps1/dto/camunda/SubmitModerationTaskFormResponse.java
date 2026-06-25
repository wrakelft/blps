package ru.itmo.blps1.dto.camunda;

public record SubmitModerationTaskFormResponse(
        String taskId,
        String moderationDecision,
        String message
) {
}