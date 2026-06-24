package ru.itmo.blps1.dto.camunda;

import java.time.LocalDateTime;

public record CamundaModerationTaskResponse(
        String taskId,
        String name,
        String assignee,
        String processInstanceId,
        Long boardId,
        Long moderationRequestId,
        LocalDateTime createdAt
) {
}