package ru.itmo.blps1.messaging.event;

import java.time.OffsetDateTime;

public record BoardModerationRequestEvent(
        Long boardId,
        Long requestedByUserId,
        String requestedByUsername,
        OffsetDateTime requestedAt
) {
}