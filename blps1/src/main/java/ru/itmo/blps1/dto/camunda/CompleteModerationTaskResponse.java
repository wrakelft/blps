package ru.itmo.blps1.dto.camunda;

public record CompleteModerationTaskResponse(
        String taskId,
        String decision,
        String message
) {
}